import subprocess


class DDlogAPI:
    def __init__(self, cmd='./routing_ddlog/target/release/routing_cli'):
        self.cmd = cmd
        self.cli = subprocess.Popen(
            cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True, shell=True)

    def apply_updates(self, updates):
        self.cli.stdin.write(updates + 'echo EOF;\n')
        self.cli.stdin.flush()

    def transaction_commit_dump(self):
        changes = []
        while True:
            line = self.cli.stdout.readline()
            if line == 'EOF\n':
                break
            changes.append(line)
        return ''.join(changes)

    def close(self):
        self.cli.stdin.close()
        self.cli.stdout.close()
        self.cli.stderr.close()
