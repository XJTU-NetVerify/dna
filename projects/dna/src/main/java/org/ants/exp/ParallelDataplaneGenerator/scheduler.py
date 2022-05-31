import re
import multiprocessing as mp
from multiprocessing.managers import BaseManager
from ddlogapi import DDlogAPI


# To `pickle` the DDlogAPI
BaseManager.register('DDlogAPI', DDlogAPI)
manager = BaseManager()
manager.start()


# Put the function out of ParallelScheduler to avoid circular reference
class Runner:
    def __init__(self):
        self.ddlogs = mp.Manager().dict()

    def exec_routing_cli(self, args):
        prefix, updates = args

        if prefix not in self.ddlogs:
            self.ddlogs[prefix] = manager.DDlogAPI()
        api = self.ddlogs[prefix]

        api.apply_updates(updates)
        ret = api.transaction_commit_dump()
        return ret

    def parse_inter_result(self, result):
        # This may be slow, but we have no transform output into input relations.
        ret = re.sub(r'(\n\w+){', lambda x: x.group(1) + '(',re.sub(r'.\w+\s=\s', '', result)).replace('}\n', ')\n').replace('}:', '):')
        ret = ret.replace('prefix_t', 'Prefix').replace('ip_t', 'Ip').split('\n')

        assert ret[-2].startswith('Timestamp')
        assert ret[0].startswith('Timestamp')
        ts_time = (int(ret[-2].split(': ')[-1]) - int(ret[0].split(' ')[-1])) / pow(10, 9)
        ret = ret[1:-2]
        return ts_time, ret

    def close(self):
        for api in self.ddlogs.values():
            api.close()


class ParallelScheduler:
    def __init__(self, processes):
        self.pool = mp.Pool(processes) # Reuse the pool for each run
        self.runner = Runner()

    def parallel_run(self, argv):
        return self.pool.map(self.runner.exec_routing_cli, argv)  # Receive results from workers as soon as they're ready

    def run(self, args):
        return self.runner.exec_routing_cli(args)

    def close(self):
        self.runner.close()
        self.pool.close()
        self.pool.join() # may not be needed
