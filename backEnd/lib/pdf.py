import subprocess
import json
import re

from copy import deepcopy
from miner import Miner


class PDF:
    def __init__(self, filename):
        self.filename = filename

    def return_from_python2_miner(self):
        args = ['python2', './lib/miner.py', self.filename]
        subprocess.call(args)

        with open('./lib/output.json', 'r') as f:
            toc = json.load(f)
            return toc

    def get_toc(self):
        toc = Miner.get_toc(self.filename)
        return toc

    def get_level(self, toc, level):
        toc_level = [x for x in toc if x['level'] == level]
        return toc_level

    def clean_toc(self, toc):
        cleaned = [x for x in toc if x['ref'] and str(x['ref']['D'])[1:].isdigit()]
        return cleaned

    def add_range(self, toc):
        new_toc = deepcopy(toc)
        for i in range(len(new_toc)):
            new_toc[i]['range'] = {
                'from': int(new_toc[i]['ref']['D'][1:]),
                'to': int(new_toc[i + 1]['ref']['D'][1:]) if (i + 1 < len(new_toc)) else 9999
            }

        return new_toc

    def add_content(self, toc):
        new_toc = deepcopy(toc)

        pagenums = []
        for t in range(len(new_toc)):
            pagenums.append(set([
                new_toc[t]['range']['from'],
                new_toc[t]['range']['to']
            ]))

        outputs = Miner.get_pages(pdf_path=self.filename, pagenums=pagenums)

        for t in range(len(new_toc)):
            clean_output = re.sub('[^A-Za-z0-9.,?!]+', ' ', outputs[t])
            new_toc[t]['content'] = clean_output

        return new_toc

    def get_summarised_data(self):
        toc = self.get_toc()
        toc_level = self.get_level(toc=toc, level=1)
        toc_level = self.clean_toc(toc_level)
        toc_level_ranged = self.add_range(toc_level)
        final_toc = self.add_content(toc_level_ranged)

        return final_toc


if __name__ == "__main__":
    '''TESTING'''
    import sys

    pdf = PDF(sys.argv[1])
    data = pdf.get_summarised_data()

    from IPython import embed

    embed()
