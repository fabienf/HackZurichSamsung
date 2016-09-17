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
                DropboxUpdate.classify_file('tmp/'+newFileMeta.name)
                
    @staticmethod
    def classify_file(file_path):
        text = Miner.get_text(file_path)
        keywords = DU.get_full_keywords_for_text(text)
        taxonomy = DU.get_single_best_taxonomy_for_text(text)
        print keywords
        print taxonomy


DropboxUpdate.cluster_all_files()


