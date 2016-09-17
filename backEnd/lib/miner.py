
import sys
import json
import re

from cStringIO import StringIO
from pdfminer.pdfparser import PDFParser
from pdfminer.pdfdocument import PDFDocument
from pdfminer.pdfpage import PDFPage
from pdfminer.pdfinterp import PDFResourceManager, PDFPageInterpreter
from pdfminer.converter import TextConverter
from pdfminer.layout import LAParams


class Miner:
    @staticmethod
    def get_toc(pdf_path):
        infile = open(pdf_path, 'rb')
        parser = PDFParser(infile)
        document = PDFDocument(parser)

        toc = list()
        for (level, title, dest, ref, structelem) in document.get_outlines():
            resolved_ref = ref.resolve()
            stringified_ref = {key: str(resolved_ref[key]) for key in resolved_ref}
            toc.append({
                'level': level,
                'title': title,
                'ref': stringified_ref
            })

        return toc

    @staticmethod
    def get_pages(pdf_path, pagenums):
        output = StringIO()
        manager = PDFResourceManager()
        converter = TextConverter(manager, output, laparams=LAParams())
        interpreter = PDFPageInterpreter(manager, converter)

        infile = open(pdf_path, 'rb')
        texts = []
        for pagenum in pagenums:
            for page in PDFPage.get_pages(infile, pagenum):
                interpreter.process_page(page)

            text = output.getvalue()
            texts.append(text)
            output.reset()
        infile.close()
        converter.close()
        output.close()

        return texts

    @staticmethod
    def get_text(pdf_path):
        pagenums = [set()]
        text = Miner.get_pages(pdf_path, pagenums)
        if not len(text):
            raise Exception('No text to search!')

        text = text[0]
        clean_text = re.sub('[^A-Za-z0-9]+', ' ', text)
        return clean_text


if __name__ == "__main__":
    '''TESTING'''
    if len(sys.argv) is not 2:
        raise Exception('Invalid number of parameters')

    # toc, parser, document, infile = Miner.get_toc(sys.argv[1])
    # with open('./lib/output.json', 'w') as f:
    #     json.dump(toc, f)

    text = Miner.get_text(sys.argv[1])
    print(text)

    # test
    from IPython import embed
    embed()
