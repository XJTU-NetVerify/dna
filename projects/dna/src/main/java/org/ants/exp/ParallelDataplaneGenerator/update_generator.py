#!/usr/bin/env python3
'''
Generate dat with kinds of updates for experiments
'''
import random
import itertools
import relation_manager


class Generator:
    def __init__(self, relations, output_path='.'):
        self.relations = relations
        self.output_path = output_path

        self.node_relations = []
        self.intf_relations = []
        self.bgp_neighbor_relations = []
        self.ospf_intf_setting_relations = []
        self.bgp_network_relations = []
        self.static_route_relations = []
        self.ospf_multipath_relations = []

        self.analyze()

    def analyze(self):
        # Get relations that will be used
        neighbor_relations = []
        for relation in self.relations:
            if isinstance(relation, relation_manager.Node):
                self.node_relations.append(relation)
            elif isinstance(relation, relation_manager.Interface):
                self.intf_relations.append(relation)
            elif (isinstance(relation, relation_manager.OspfNeighbor) or
                isinstance(relation, relation_manager.BgpNeighbor) or
                isinstance(relation, relation_manager.IBgpNeighbor)):
                neighbor_relations.append(relation)
                if isinstance(relation, relation_manager.BgpNeighbor):
                    self.bgp_neighbor_relations.append(relation)
            elif isinstance(relation, relation_manager.OspfIntfSetting):
                self.ospf_intf_setting_relations.append(relation)
            elif isinstance(relation, relation_manager.BgpNetwork):
                self.bgp_network_relations.append(relation)
            elif isinstance(relation, relation_manager.StaticRoute):
                self.static_route_relations.append(relation)
            elif isinstance(relation, relation_manager.OspfMultipath):
                self.ospf_multipath_relations.append(relation)

        # Replace interface name in ospf/bgpneighbor relations with its ip
        intf_to_ip = {}
        for relation in self.intf_relations:
            intf = '{}-{}'.format(relation.node, relation.intf)
            intf_to_ip[intf] = '{}'.format(relation.prefix.ip)
        for relation in neighbor_relations:
            # relation.ip here may be the interface name
            intf1 = '{}-{}'.format(relation.node1, relation.ip1)
            intf2 = '{}-{}'.format(relation.node2, relation.ip2)
            if intf1 in intf_to_ip:
                relation.ip1 = intf_to_ip[intf1]
            if intf2 in intf_to_ip:
                relation.ip2 = intf_to_ip[intf2]

    def generate_node_failure_update(self, batch_size=100):
        update = []
        restore = []
        for _ in range(batch_size):
            node = random.choice(self.node_relations)
            node_relations = []
            for relation in self.relations:
                if (hasattr(relation, 'node') and relation.node == node.node or
                    hasattr(relation, 'node1') and relation.node1 == node.node):
                    node_relations.append(relation)
            update.append(''.join(('delete {},\n'.format(x) for x in node_relations)))
            restore.append(''.join(('insert {},\n'.format(x) for x in node_relations)))
        return update, restore


    def generate_link_failure(self, links, k):
        updates = []
        restores = []
        intf_dict = {}
        for intf in self.intf_relations:
            intf_dict[(intf.node.device, intf.intf)] = intf

        for comb in itertools.combinations(links, k):
            delete, restore = '', ''
            for link in comb:
                delete += 'delete {},\ndelete {},\n'.format(intf_dict[(link[0], link[1])], intf_dict[(link[2], link[3])])
                restore += 'insert {},\ninsert {},\n'.format(intf_dict[(link[0], link[1])], intf_dict[(link[2], link[3])])
            updates.append(delete)
            restores.append(restore)
        return updates, restores
