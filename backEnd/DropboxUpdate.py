import dropbox


class DropboxUpdate

dbx = dropbox.Dropbox('F2_PGWfw-GAAAAAAAAAACqNZoyJNzdLMd7x-BKLsGSE7hHM07KRMfT6jJgtWLgub')


print dbx.users_get_current_account()

try:
	dbx.files_create_folder('/dog')
except :
    pass
dbx.files_move('/HamesIM.pdf','/dog/HamesIM.pdf')


newFileMeta = None

for entry in dbx.files_list_folder('').entries:
    print(entry.name)
    if '.pdf' in entry.name:
    	newFileMeta = entry
    	break



print(newFileMeta.path_lower)
newFile = dbx.files_download(newFileMeta.path_lower)