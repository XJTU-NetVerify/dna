import time

class Timer:
    def __init__(self, name):
        self.name = name
        self._start_time = None

    def start(self):
        self._start_time = time.perf_counter()

    def stop(self):
        elapsed_time = time.perf_counter() - self._start_time
        self._start_time = None
        print("{}: {:0.4f} s".format(self.name, elapsed_time))

    def __enter__(self):
        self.start()
        return self

    def __exit__(self, *exc_info):
        self.stop()