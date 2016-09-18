import dropbox
from DocumentUnderstanding import DocumentUnderstanding as DU
from lib.miner import Miner



class DropboxUpdate:

    @staticmethod
    def cluster_all_files():
        #sign in
        dbx = dropbox.Dropbox('F2_PGWfw-GAAAAAAAAAACqNZoyJNzdLMd7x-BKLsGSE7hHM07KRMfT6jJgtWLgub')
        #debug info

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
        folder_names = {'/law, govt and politics' : 'Law Govt and Politics',
            '/science' : 'Science',
            '/business and industrial' : 'Business and Industrial',
            '/art and entertainment' : 'Art and Entertainment',
            '/education' : 'Education',
            '/finance' : 'Finance',
            '/hobbies and interests' : 'Hobbies and Interests',
            '/news' : 'News',
            '/sports' : 'Sports',
            '/technology and computing' : 'Technology and Computing',
            '/health and fitness' : "Health and Fitness",
            '/travel' : 'Travel'
            }
        #try to create a folder
        folder_name = '/' + taxonomy.split('/')[1]
        print folder_name
        if folder_name in folder_names:
            folder_name = folder_names[folder_name]
        else:
            folder_name = folder_name.split('/')[1]

        try:
            dbx.files_create_folder('/'+folder_name)
        except :
            pass
        dbx.files_move('/'+file_name,'/'+folder_name+'/'+file_name)
        print "File moved sucesfully"


#DropboxUpdate.cluster_all_files()


