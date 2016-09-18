from flask import Flask, request, redirect, url_for, json, render_template, jsonify, send_from_directory
from lib.pdf import PDF
from lib.miner import Miner

from lib.DocumentUnderstanding import DocumentUnderstanding as DU
from lib.DropboxUpdate import DropboxUpdate
import pickle
import os
import uuid
import pprint

app = Flask(__name__)
app.debug = True
pp = pprint.PrettyPrinter(depth=6)
port = int(os.getenv('VCAP_APP_PORT', 8080))

stored_files = {
    'test': './tmp/HamesIM.pdf',
    'wto': './data/wto.pdf',
    'dd': './data/dd.pdf',
    'tablet': './data/tablet.pdf',
    'hpstone': './data/hpstone.pdf'
}


@app.route('/', methods=['GET'])
def hello_world():
    filename = './test/data/tablet.pdf'
    pdf = PDF(filename)
    result = pdf.get_summarised_data()
    pretty_text = pp.pformat(result)
    # return (pretty_text)
    return send_from_directory(directory='tmp', filename='HamesIM.pdf')


def save_file():
    if 'file' not in request.files:
        print('No file part')
        return 'No file part'
    file = request.files['file']

    # if user does not select file, browser also
    # submit a empty part without filename
    if file.filename == '':
        print('No selected file')
        return 'No selected file'

    file_path = './tmp/' + file.filename
    file_uuid = str(uuid.uuid4())
    stored_files[file_uuid] = file_path

    print(file_path)
    file.save(file_path)
    return file_path, file_uuid


def process_file_data(file_name, file_data, file_uuid):
    data_out_parts = []
    for chapter in file_data:
        keywords = DU.get_full_keywords_for_text(chapter['content'])
        summary = DU.get_summary_for_text(chapter['content'])
        pages = [chapter['range']['from'], chapter['range']['to']]
        line = {'name': chapter['title'], 'description': summary, 'keys': keywords, 'pages': pages}
        data_out_parts.append(line)
        print(line)

    return {
        'id': file_uuid,
        'file': file_name,
        'parts': data_out_parts
    }

@app.route('/check', methods=['POST'])
def check_file():
    content = request.get_json()
    filename = content['filename']

    root = filename.lower().split('.pdf')[0]
    file_path = None
    for key in stored_files.keys():
        if key.lower() == root:
            file_path = (key, stored_files[key])
            break

    if not file_path:
        return "None"

    pdf = PDF(file_path[1])
    result = pdf.get_summarised_data()
    return jsonify(process_file_data(
        file_name=filename,
        file_data=result,
        file_uuid=file_path[0]
    ))


@app.route('/upload', methods=['POST'])
def upload_file():
    # save file to the disk if it exists, otherwise return error
    file_path, file_uuid = save_file()
    if ('No file part' or 'No selected file') in file_path:
        return file_path

    # file_path = 'test/data/tablet.pdf'

    if 'tablet.pdf' in file_path:
        pdf = PDF('./' + file_path)
        result = pdf.get_summarised_data()
        return jsonify(process_file_data(
            file_name=file_path,
            file_data=result,
            file_uuid=file_uuid
        ))
    else:
        with open('./test/test.pickle', 'rb') as f:
            json_file = pickle.load(f)
            return jsonify(process_file_data(
                file_name=file_path,
                file_data=json_file,
                file_uuid="test"
            ))


            # with open('return_data.json') as data_file:
            #     data = json.load(data_file)
            # return jsonify(data)


@app.route('/range', methods=['POST'])
def split_file():
    content = request.get_json()
    ranges = content['pages']
    if not len(ranges):
        print('Empty range!')
        return 'Empty range!'

    if content['id'] not in stored_files:
        print('File not found!')
        return 'File not found!'

    file_path = stored_files[content['id']]
    directory_path, file_name = Miner.extract_pages(pdf_path=file_path, ranges=ranges)

    return send_from_directory(directory=directory_path, filename=file_name)

@app.route('/dropbox', methods=['GET'])
def cluser_files():
    DropboxUpdate.cluster_all_files()
    return "success"


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=port)
