import os
import sys
import time
from enum import Enum
from collections import defaultdict

import networkx as nx

from timer import Timer
from scheduler import ParallelScheduler

import relation_manager
from update_generator import Generator


class PrefixType(Enum):
    Ospf = "ospf"
    Bgp = "bgp"


class Prefix:
    def __init__(self, type, prefix):
        self.type = type
        self.prefix = prefix

    def __repr__(self):
        return 'type: {}, prefix: {}'.format(self.type, self.prefix)


class ParallelRunner():
    def __init__(self, path, processes, groups):
        self.groups = groups
        self.path = path
        self.processes = processes
        self.relations = relation_manager.read_relations(path)
        self.dep_graph = nx.DiGraph()
        self.scheduler = ParallelScheduler(processes)

        # to support separately run
        self.ospf_relations = []
        self.bgp_relations = []
        self.ospf_networks = set()
        self.bgp_networks = set()
        self.prefix_groups = []

        # to generate random update
        self.generator = Generator(self.relations)
        self.mode = 'None'

        self.records = {}

    def get_outer_bgp_prefixes(self, current_as, bgp_as_to_nodes, bgp_node_to_networks):
        prefixes = []
        for as_number, nodes in bgp_as_to_nodes.items():
            if as_number != current_as:
                for node in nodes:
                    for prefix in bgp_node_to_networks[node]:
                        prefixes.append(prefix)
        return prefixes

    def analyze_dep(self):
        """Get dependency among prefixes, currently only consider Ospf as IGP protocol

        dependency:
        1) IGP -> BGP
           a. bgp network, redistribute
           b. bgp neighbors: bgp networks of other ASes depend on current neighbor interfaces
           c. next_hop/global rib: bgp networks from a ebgp router(ASBR) depend on the prefix of the interface on the router
        2) Aggregation
        """
        # collect informations
        ip_to_prefix = {}
        bgp_node_to_as = {}
        bgp_as_to_nodes = defaultdict(list)
        bgp_node_to_networks = defaultdict(list)
        for relation in self.relations:
            if isinstance(relation, relation_manager.Node):
                bgp_node_to_as[relation.node.device] = relation.as_number
                bgp_as_to_nodes[relation.as_number].append(relation.node.device)
            elif isinstance(relation, relation_manager.Interface):
                prefix = Prefix(PrefixType.Ospf, relation.prefix)
                ip_to_prefix[relation.prefix.ip] = prefix
                self.ospf_networks.add(prefix.prefix.net)
            elif isinstance(relation, relation_manager.BgpNetwork):
                prefix = Prefix(PrefixType.Bgp, relation.prefix)
                bgp_node_to_networks[relation.node.device].append(prefix)
                self.bgp_networks.add(prefix.prefix.net)
            else:
                pass

            if issubclass(type(relation), relation_manager.OspfRelation):
                self.ospf_relations.append(relation)
            elif issubclass(type(relation), relation_manager.BgpRelation):
                self.bgp_relations.append(relation)
            else:
                pass

        if len(self.ospf_relations) > 0:
            self.mode = 'ospf'
        elif len(self.bgp_relations) > 0:
            self.mode = 'bgp'

        # we currently just run as base -> ospf -> bgp, so don't need to analyze deps
        return

        # # analyze deps
        # for relation in self.relations:
        #     if isinstance(relation, relation_manager.BgpNetwork):
        #         dep_prefix = Prefix(PrefixType.Ospf, relation.prefix)
        #         prefix = Prefix(PrefixType.Bgp, relation.prefix)
        #         self.dep_graph.add_edge(dep_prefix, prefix)
        #     elif isinstance(relation, relation_manager.BgpRedis):
        #         pass
        #     elif isinstance(relation, relation_manager.IBgpNeighbor):
        #         dep_prefix1 = Prefix(PrefixType.Ospf, ip_to_prefix[relation.ip1])
        #         dep_prefix2 = Prefix(PrefixType.Ospf, ip_to_prefix[relation.ip2])
        #         current_as = bgp_node_to_as[relation.node1.device]
        #         for prefix in self.get_outer_bgp_prefixes(
        #             current_as, bgp_as_to_nodes, bgp_node_to_networks):
        #             self.dep_graph.add_edge(dep_prefix1, prefix)
        #             self.dep_graph.add_edge(dep_prefix2, prefix)
        #     elif isinstance(relation, relation_manager.BgpNeighbor):
        #         # relation.ip1 and relation.ip2 should in the same subnet, so we can just pick one
        #         assert ip_to_prefix[relation.ip1].prefix == ip_to_prefix[relation.ip2].prefix
        #         dep_prefix1 = Prefix(PrefixType.Ospf, ip_to_prefix[relation.ip1])
        #         as1 = bgp_node_to_as[relation.node1.device]
        #         for prefix in self.get_outer_bgp_prefixes(
        #             as1, bgp_as_to_nodes, bgp_node_to_networks):
        #             self.dep_graph.add_edge(dep_prefix1, prefix)

        #         as2 = bgp_node_to_as[relation.node2]
        #         for prefix in self.get_outer_bgp_prefixes(
        #             as2, bgp_as_to_nodes, bgp_node_to_networks):
        #             self.dep_graph.add_edge(dep_prefix1, prefix)
        #     else:
        #         pass

    def run(self):
        start_time = time.perf_counter()

        if self.mode == 'ospf':
            networks = self.ospf_networks
            enable = 'insert OspfPrefixSpreadEnable({}),\n'
        elif self.mode == 'bgp':
            networks = self.bgp_networks
            enable = 'insert BgpPrefixSpreadEnable({}),\n'
        else:
            raise Exception('not BGP and not OSPF configs')

        self.groups = min(len(networks), self.groups)
        self.prefix_groups = list(self.split_into_n_chunks(networks, self.groups))

        head = 'start;\n'
        tail = 'commit;\n' + 'dump Fib;\n'
        group_inputs = []
        pub_inputs = head + ''.join(('insert {},\n'.format(x) for x in self.relations))
        for prefixes in self.prefix_groups:
            key = ''.join(prefixes)
            prefix_inputs = ''.join((enable.format(x) for x in prefixes))
            dat = pub_inputs + prefix_inputs + tail
            group_inputs.append((hash(key), dat))

        # with Timer('run time'):
        results = self.scheduler.parallel_run(group_inputs)

        fibs = []
        for result in results:
            fibs.extend(result.split('\n'))
        fibs = set(fibs)
        # for fib in fibs:
        #     print(fib)

        end_time = time.perf_counter()
        self.records['snapshot'] = (end_time - start_time, len(fibs))

    def run_node_failure_update(self):
        self.update_test(*self.generator.generate_node_failure_update())

    def update_test(self, update_relations, restore_relations):
        assert len(update_relations) == len(restore_relations)
        for i in range(len(update_relations)):
            run_time, fibs = self.run_update(update_relations[i])
            self.records['update_{}'.format(i)] = (run_time, len(fibs))
            # print('Epoch-update: {}, Time: {}, Fibs: {}'.format(i, run_time, len(fibs)))
            run_time, fibs = self.run_update(restore_relations[i])
            self.records['restore_{}'.format(i)] = (run_time, len(fibs))
            # print('Epoch-restore: {}, Time: {}, Fibs: {}'.format(i, run_time, len(fibs)))

    def run_update(self, updates):
        start_time = time.perf_counter()

        head = 'start;\n'
        tail = 'commit dump_changes;\n'
        dat = head + updates + tail
        group_inputs = []
        for prefixes in self.prefix_groups:
            key = ''.join(prefixes)
            group_inputs.append((hash(key), dat))

        # with Timer('run time'):
        results = self.scheduler.parallel_run(group_inputs)

        fibs = set()
        for result in results:
            fibs.update(result.split('\n'))

        end_time = time.perf_counter()
        return end_time - start_time, fibs

    def generate_output(self, path='./', round=0):
        filename = '{}-p{}-g{}-r{}.dump'.format(self.mode, self.processes, self.groups, round)
        update_times = [v[0] for k, v in self.records.items() if k.startswith('update')]
        update_fibs = [v[1] for k, v in self.records.items() if k.startswith('update')]
        avg_update_times = sum(update_times) / len(update_times)
        avg_update_fibs = int(sum(update_fibs) / len(update_fibs))

        restore_times = [v[0] for k, v in self.records.items() if k.startswith('restore')]
        restore_fibs = [v[1] for k, v in self.records.items() if k.startswith('restore')]
        avg_restore_times = sum(restore_times) / len(restore_times)
        avg_restore_fibs = int(sum(restore_fibs) / len(restore_fibs))

        records = '{}, {}\n'.format(*self.records['snapshot']) + \
                 '{}, {}\n'.format(avg_update_times, avg_update_fibs) + \
                 '{}, {}\n'.format(avg_restore_times, avg_restore_fibs) + \
                 ''.join(['{}, {}\n'.format(*v) for k, v in self.records.items() if k.startswith('update')]) + \
                 ''.join(['{}, {}\n'.format(*v) for k, v in self.records.items() if k.startswith('restore')])
        with open(os.path.join(path, filename), 'w') as file:
            file.write(records)

    # number of chunks is n
    def split_into_n_chunks(self, seq, n):
        seq = list(seq)
        k, m = divmod(len(seq), n)
        return (seq[i * k + min(i, m):(i + 1) * k + min(i + 1, m)] for i in range(n))

    # size of each chunk is n
    def split_as_n_chunks(self, seq, n):
        seq = list(seq)
        return (seq[i:i+n] for i in range(0, len(seq), n))

    def flat_map(self, keys, values):
        m = defaultdict(list)
        if isinstance(keys, str):
            for v in values:
                m[keys].append(v)
        else:
            for i, k in enumerate(keys):
                for v in values:
                    m[k].append(v[i])
        return m

    def close(self):
        self.scheduler.close()


def test(path):
    runner = ParallelRunner(path, 24, 24)
    runner.analyze_dep()
    runner.run()
    runner.run_node_failure_update()
    runner.generate_output()
    runner.close()


if __name__ == "__main__":
    if len(sys.argv) == 2:
        path = sys.argv[1]
        test(path)
