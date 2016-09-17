
import json

from pdfminer.pdfparser import PDFParser
from pdfminer.pdfdocument import PDFDocument

def get_toc(pdf_path):
    infile = open(pdf_path, 'rb')
    parser = PDFParser(infile)
    document = PDFDocument(parser)

    toc = list()
    for (level,title,dest,a,structelem) in document.get_outlines():
        toc.append((level, title))

    return toc

if __name__ == "__main__":
    filename = 'test/data/hpstone.pdf'
    text = get_toc(filename)
    with open('lib/output.json', 'w') as f:
        json.dump(text, f)
