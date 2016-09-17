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

from PyPDF2 import PdfFileWriter, PdfFileReader


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
        clean_text = re.sub('[^A-Za-z0-9.,?!]+', ' ', text)
        return clean_text

    @staticmethod
    def extract_pages(pdf_path, ranges=None):
        if not ranges:
            raise Exception('No page range specified!')

        with open(pdf_path, 'rb') as f:
            infile = PdfFileReader(f)

            outfile = PdfFileWriter()
            for range in ranges:
                from_page = int(range[0])
                to_page = int(range[1])
                if from_page > to_page:
                    raise Exception('Wrong range format!')

                for i in xrange(from_page, to_page + 1):
                    p = infile.getPage(i)
                    outfile.addPage(p)

            directory_path, file_name = './tmp/', 'output.pdf'
            with open(directory_path + file_name, 'wb') as fw:
                outfile.write(fw)

            return directory_path, file_name


if __name__ == "__main__":
    '''TESTING'''
    if len(sys.argv) is not 2:
        raise Exception('Invalid number of parameters')

    # toc, parser, document, infile = Miner.get_toc(sys.argv[1])
    # with open('./lib/output.json', 'w') as f:
    #     json.dump(toc, f)

    # test: extract pages
    # ranges = [
    #     [1,5],
    #     [19,40]
    # ]
    # text = Miner.extract_pages(sys.argv[1], ranges)

    # test
    from IPython import embed

    embed()
