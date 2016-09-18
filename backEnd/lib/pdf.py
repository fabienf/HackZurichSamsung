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
        toc, document = Miner.get_toc(self.filename)
        return toc, document

    def get_level(self, toc, level):
        toc_level = [x for x in toc if x['level'] == level]
        return toc_level

    def add_range(self, toc):
        new_toc = deepcopy(toc)
        for i in range(len(new_toc)):
            new_toc[i]['range'] = {
                'from': int(new_toc[i]['ref']['D']),
                'to': int(new_toc[i + 1]['ref']['D']) if (i + 1 < len(new_toc)) else 9999
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

    def pagify(self, toc, pages):
        def find_page(d):
            if str(d[1:]).isdigit():
                return int(d[1:])

            if 'PDFObjRef' in str(d):
                suffix = str(d).split('PDFObjRef')[1]
                m = re.search("\d+", suffix)
                objid = int(m.group())
                return pages[objid]

            return None

        new_toc = []

        for i in xrange(len(toc)):
            page = find_page(toc[i]['ref']['D'])
            if page:
                new_obj = deepcopy(toc[i])
                new_obj['ref']['D'] = page
                new_toc.append(new_obj)

        return new_toc

    def get_summarised_data(self):
        print('Analysing data...')
        pages = Miner.get_page_objids(pdf_path=self.filename)
        print('[1/6]')
        toc, document = self.get_toc()
        print('[2/6]')
        toc_level = self.get_level(toc=toc, level=1)
        print('[3/6]')

        # from IPython import embed
        # embed()
        # sys.exit()

        toc_level_pages = self.pagify(toc_level, pages)
        print('[4/6]')
        toc_level_ranged = self.add_range(toc_level_pages)
        print('[5/6]')
        final_toc = self.add_content(toc_level_ranged)
        print('[6/6]')

        return final_toc


if __name__ == "__main__":
    '''TESTING'''
    import sys

    pdf = PDF(sys.argv[1])
    data = pdf.get_summarised_data()

    from IPython import embed
    embed()
