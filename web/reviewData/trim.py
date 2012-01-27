#!/usr/bin/env python

import sys
import pickle
import json

if __name__ == '__main__':
	if (len(sys.argv) != 3):
		print 'USAGE: python trim.py <sourcePickleFile> <destJsonFile>'
		sys.exit(-1)

	sourcePickleFile = open(sys.argv[1], 'r')
	destJsonFile = open(sys.argv[2], 'wb')
	
	# data is a dictionary of place_name -> dictionary of reviews
	print 'loading ' + sys.argv[1]
	data = pickle.load(sourcePickleFile)
	print 'done loading ' + sys.argv[1]

	print 'trimming all the reviews'
	for key in data.keys():
		for key2 in data[key].keys():
			data[key].pop(key2)
	print 'done trimming reviews'

	print 'dumping json output'
	json.dump(data, destJsonFile)
	print 'json output dump complete'