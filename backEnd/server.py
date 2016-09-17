from flask import Flask, request, redirect, url_for, json, render_template, jsonify
from lib.pdf import PDF

import json
import os
import sys
import pprint

app = Flask(__name__)
pp = pprint.PrettyPrinter(depth=6)
port = int(os.getenv('VCAP_APP_PORT', 8080))

json_file = ''


@app.route('/')
def hello_world():
	print(sys.version)
	print('Hello World! I am running on port ' + str(port))

	tx = PDF.get_text()
	text = pp.pformat(tx)
	return (text)

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
	print(file.filename)
	file.save(file.filename)
	return None

@app.route('/upload', methods=['POST'])
def upload_file():
	# save file to the disk if it exists, otherwise return error
	save_success = save_file()
	if save_success:
		return save_success

	with open('return_data.json') as data_file:
		data = json.load(data_file)
	return jsonify(data)



if __name__ == '__main__':
	app.run(host='0.0.0.0', port=port)
