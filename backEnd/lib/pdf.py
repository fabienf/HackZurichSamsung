
import subprocess
import json


class PDF:

    def __init__(self):
        pass

    def get_text(self):
        args = ['python2', 'lib/miner.py']
        subprocess.call(args)

        with open('lib/output.json', 'r') as f:
            text = json.load(f)
            return text

if __name__ == "__main__":
    args = ['python2', 'miner.py']
    subprocess.call(args)

    with open('lib/output.json', 'r') as f:
        text = json.load(f)
        print(text)