import logging
from pdfminer.psparser import LIT
from pdfminer.pdftypes import resolve1
from pdfminer.pdftypes import int_value
from pdfminer.pdftypes import list_value
from pdfminer.pdftypes import dict_value
from pdfminer.pdfparser import PDFParser
from pdfminer.pdfdocument import PDFDocument
from pdfminer.pdfdocument import PDFTextExtractionNotAllowed

# some predefined literals and keywords.
LITERAL_PAGE = LIT('Page')
LITERAL_PAGES = LIT('Pages')

pageno = 0
result_pages = []


class PDFPage(object):
    debug = False

    def __init__(self, doc, pageid, attrs):
        self.doc = doc
        self.pageid = pageid
        self.attrs = dict_value(attrs)
        self.lastmod = resolve1(self.attrs.get('LastModified'))
        self.resources = resolve1(self.attrs.get('Resources', dict()))
        self.mediabox = resolve1(self.attrs['MediaBox'])
        if 'CropBox' in self.attrs:
            self.cropbox = resolve1(self.attrs['CropBox'])
        else:
            self.cropbox = self.mediabox
        self.rotate = (int_value(self.attrs.get('Rotate', 0)) + 360) % 360
        self.annots = self.attrs.get('Annots')
        self.beads = self.attrs.get('B')
        if 'Contents' in self.attrs:
            contents = resolve1(self.attrs['Contents'])
        else:
            contents = []
        if not isinstance(contents, list):
            contents = [contents]
        self.contents = contents
        return

    def __repr__(self):
        return '<PDFPage: Resources=%r, MediaBox=%r>' % (self.resources, self.mediabox)

    INHERITABLE_ATTRS = set(['Resources', 'MediaBox', 'CropBox', 'Rotate'])

    @classmethod
    def create_pages(klass, document):
        global pageno
        global result_pages
        result_pages = []
        pageno = 0

        def search(obj, parent):
            global pageno
            global result_pages
            if isinstance(obj, int):
                objid = obj
                tree = dict_value(document.getobj(objid)).copy()
            else:
                objid = obj.objid
                tree = dict_value(obj).copy()
            for (k, v) in parent.iteritems():
                if k in klass.INHERITABLE_ATTRS and k not in tree:
                    tree[k] = v
            if tree.get('Type') is LITERAL_PAGES and 'Kids' in tree:
                if klass.debug: logging.info('Pages: Kids=%r' % tree['Kids'])
                for c in list_value(tree['Kids']):
                    for x in search(c, tree):
                        yield x
            elif tree.get('Type') is LITERAL_PAGE:
                if klass.debug: logging.info('Page: %r' % tree)
                pageno += 1
                result_pages.append((objid, pageno))
                yield (objid, tree, pageno)

        pages = False
        if 'Pages' in document.catalog:
            for (objid, tree, pageno) in search(document.catalog['Pages'], document.catalog):
                pass
            return result_pages

        return None

    @classmethod
    def get_pages(klass, fp,
                  pagenos=None, maxpages=0, password=b'',
                  caching=True, check_extractable=True):
        # Create a PDF parser object associated with the file object.
        parser = PDFParser(fp)
        # Create a PDF document object that stores the document structure.
        doc = PDFDocument(parser, password=password, caching=caching)
        # Check if the document allows text extraction. If not, abort.
        if check_extractable and not doc.is_extractable:
            raise PDFTextExtractionNotAllowed('Text extraction is not allowed: %r' % fp)
        # Process each page contained in the document.

        pages = klass.create_pages(doc)
        return pages
