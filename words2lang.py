#words2lang.py

'''
The MIT License (MIT)

Copyright (c) 2020 Joseph Bettendorff a.k.a. "Commoble"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
'''

'''
Util for converting nameword files to lang json entries.
'''
import argparse
import string
import os
import os.path
import json
from pathlib import Path

'''
Returns a dict of headered paths, e.g. the absolute path
**/data/modid/looot/namewords/folder/subfolder/name.json becomes
"modid.looot.namewords.folder.subfolder.name" : path
Paths without the looot/namewords folder are filtered out and not included
'''
def getHeaderedFiles(paths: list) -> dict:
	out = dict()
	for path in paths:
		parts = path.parts
		pathSize = len(parts)
		for i in range(pathSize):
			if parts[i] == "data" and i+3 < pathSize and parts[i+2] == "looot" and parts[i+3] == "namewords":
				header = makeHeader(parts[i+1:])
				out[header] = path
				break

	return out

noCaps = set(["of", "the"])

# convert e.g. "of_the_thing" to "of the Thing"
def translate(words: str) -> str:
	return " ".join([word if word in noCaps else word.capitalize() for word in words.split("_")])


# converts ["a", "b", "c", "thing.json"] to "a.b.c.thing"
def makeHeader(parts: list) -> str:
	unsuffixedJsonName = parts[-1][0:-5] # converts "thing.json" to "thing"
	subComponents = [x for x in parts[0:-1]] + [unsuffixedJsonName]
	return ".".join(subComponents)

def getInputJson(path) -> dict:
	with open(path, "r") as inFile:
		inputJson = json.load(inFile) # loads json object as a python Dict
	return inputJson

parser = argparse.ArgumentParser(description="Generate lang json entries from looot nameword data files")
parser.add_argument("--path", type=str, dest="path",
	default="src/main/resources/data/",
	help="Relative path fom the current folder to the data folder to read")
parser.add_argument("--output", type=str, dest="output",
	default="src/main/resources/assets/looot-generated-words/lang/en_us.json",
	help="Relative path of the output file (will be replaced if present)")

args = parser.parse_args()
cwd = os.getcwd()
dataFolder = os.path.join(cwd, args.path)
outputPath = os.path.join(cwd, args.output)
# we need a list of all of the jsons in the data/[modid]/looot/namewords/ folder, for all modids
# we also need to remember which modid each of these jsons are under
# we want to be able to convert e.g. data/forge/looot/namewords/suffixes/rods/fishing.json
# to some identifier that includes at minimum the modid ("forge" in this example) + everything after namewords
# "forge.looot.namewords.suffixes.rods.fishing" would be the simplest format here
dataDomains = [folder.path for folder in os.scandir(dataFolder) if folder.is_dir()]
# nameWordFolders = [os.path.join(domainFolder, nameWordFolder) for domainFolder in dataDomains]
# nameWordFolders = [f for f in nameWordFolders if os.path.exists(f)]
# now we have a list of all of the absolute paths of the nameword folders (under any modid) in the workspace
# the next thing we want to do is to get *all* of the json files in these folders, along with the relative paths to them
pathsToJsons = list(Path(".").rglob("*.json"))
headeredFiles = getHeaderedFiles(pathsToJsons)
entries = dict()
for header in headeredFiles:
	path = headeredFiles[header]
	inputJson = getInputJson(path) # gets json object as a python Dict
	values = inputJson["values"] # a list
	for value in values:
		key = '.'.join([header,value])
		entries[key] = translate(value)

os.makedirs(os.path.dirname(outputPath), exist_ok=True)
with open(outputPath, "w+") as outFile:
	json.dump(entries, outFile, indent='\t')

print("Generated " + str(len(entries)) + " entries in " + outputPath)
