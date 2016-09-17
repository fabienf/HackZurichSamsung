import dropbox
from DocumentUnderstanding import DocumentUnderstanding as DU
from lib.miner import Miner



class DropboxUpdate:

    @staticmethod
    def cluster_all_files():
        #sign in
        dbx = dropbox.Dropbox('F2_PGWfw-GAAAAAAAAAACqNZoyJNzdLMd7x-BKLsGSE7hHM07KRMfT6jJgtWLgub')
        #debug info
        #print dbx.users_get_current_account()

        #
        try:
            dbx.files_create_folder('/dog')
        except :
            pass
        #dbx.files_move('/HamesIM.pdf','/dog/HamesIM.pdf')


        newFileMeta = None

        for entry in dbx.files_list_folder('').entries:
            print(entry.name)
            if '.pdf' in entry.name:
                newFileMeta = entry
                print(newFileMeta.path_lower)
                newFile = dbx.files_download_to_file('tmp/'+newFileMeta.name,newFileMeta.path_lower)
                DropboxUpdate.classify_file(dbx,'tmp/'+newFileMeta.name,newFileMeta.name)
                
    @staticmethod
    def classify_file(dbx,file_path,file_name):
        text = Miner.get_text(file_path)
        #keywords = DU.get_full_keywords_for_text(text)
        taxonomy = DU.get_single_best_taxonomy_for_text(text)
        #print keywords
        print taxonomy
        folder_names = {'/law, govt and politics' : 'Law Govt and Politics', '/science' : 'Science', '/business and industrial' : 'Business and Industrial'}
        print folder_names[taxonomy]
        #try to create a folder
        try:
            dbx.files_create_folder('/'+folder_names[taxonomy])
        except :
            pass
        dbx.files_move('/'+file_name,'/'+folder_names[taxonomy]+'/'+file_name)
        print "File moved sucesfully"


DropboxUpdate.cluster_all_files()


