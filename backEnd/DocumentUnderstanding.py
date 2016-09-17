import json
from watson_developer_cloud import AlchemyLanguageV1
from gensim.summarization import summarize, keywords

class DocumentUnderstanding:

	@staticmethod
	def get_full_keywords_for_text(my_text):
		alchemy_language = AlchemyLanguageV1(api_key='9294e74109f6f577c9d34be2a6ad120f1c0e0214')
		return alchemy_language.keywords(text = my_text, max_items = 5)

	@staticmethod
	def get_single_best_keywords_for_text(my_text):
		alchemy_language = AlchemyLanguageV1(api_key='9294e74109f6f577c9d34be2a6ad120f1c0e0214')
		return alchemy_language.keywords(text = my_text, max_items = 1)['keywords'][0]['text']
		  

	@staticmethod
	def get_summary_for_text(my_text):
		return summarize(my_text, word_count=50)

	@staticmethod
	def get_keywords_for_text2(my_text):
		return keywords(my_text)

	@staticmethod
	def get_full_taxonomy_for_text(my_text):
		alchemy_language = AlchemyLanguageV1(api_key='9294e74109f6f577c9d34be2a6ad120f1c0e0214')
		return alchemy_language.taxonomy(text = my_text)

	@staticmethod
	def get_single_best_taxonomy_for_text(my_text):
		alchemy_language = AlchemyLanguageV1(api_key='9294e74109f6f577c9d34be2a6ad120f1c0e0214')
		return alchemy_language.taxonomy(text = my_text)['taxonomy'][0]['label']
		  

my_text = "Law is a system of rules that are enforced through social institutions to govern behavior.[2] Laws can be made by a collective legislature or by a single legislator, resulting in statutes, by the executive through decrees and regulations, or by judges through binding precedent, normally in common law jurisdictions. Private individuals can create legally binding contracts, including arbitration agreements that may elect to accept alternative arbitration to the normal court process. The formation of laws themselves may be influenced by a constitution, written or tacit, and the rights encoded therein. The law shapes politics, economics, history and society in various ways and serves as a mediator of relations between people.\r\n\r\nA general distinction can be made between (a) civil law jurisdictions (including Catholic canon law and socialist law), in which the legislature or other central body codifies and consolidates their laws, and (b) common law systems, where judge-made precedent is accepted as binding law. Historically, religious laws played a significant role even in settling of secular matters, which is still the case in some religious communities, particularly Jewish, and some countries, particularly Islamic. Islamic Sharia law is the world\'s most widely used religious law.[3]\r\n\r\nThe adjudication of the law is generally divided into two main areas referred to as (i) Criminal law and (ii) Civil law. Criminal law deals with conduct that is considered harmful to social order and in which the guilty party may be imprisoned or fined. Civil law (not to be confused with civil law jurisdictions above) deals with the resolution of lawsuits (disputes) between individuals or organizations.[4]\r\n\r\nLaw provides a rich source of scholarly inquiry into legal history, philosophy, economic analysis and sociology. Law also raises important and complex issues concerning equality, fairness, and justice. There is an old saying that \'all are equal before the law\', although Jonathan Swift argued that \'Laws are like cobwebs, which may catch small flies, but let wasps and hornets break through.\' In 1894, the author Anatole France said sarcastically, \"In its majestic equality, the law forbids rich and poor alike to sleep under bridges, beg in the streets, and steal loaves of bread.\"[5] Writing in 350 BC, the Greek philosopher Aristotle declared, \"The rule of law is better than the rule of any individual.\"[6] Mikhail Bakunin said: \"All law has for its object to confirm and exalt into a system the exploitation of the workers by a ruling class\".[7] Cicero said \"more law, less justice\".[8] Marxist doctrine asserts that law will not be required once the state has withered away.[9] Regardless of one\'s view of the law, it remains today a completely central institution."


print DocumentUnderstanding.get_single_best_keywords_for_text(my_text)
#print json.dumps(DocumentUnderstanding.get_keywords_for_text(my_text),indent=2)
#print DocumentUnderstanding.get_keywords_for_text2(my_text)
#print DocumentUnderstanding.get_summary_for_text(my_text)
#print json.dumps(DocumentUnderstanding.get_taxonomy_for_text(my_text),indent=2)
