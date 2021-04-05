import pandas as pd
import numpy as np
import pandas as pd
import numpy as np
from nltk.corpus import stopwords
from sklearn.metrics.pairwise import linear_kernel
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfVectorizer
from nltk.tokenize import RegexpTokenizer
import nltk
nltk.download('stopwords')
import re
import string
import random
from PIL import Image
import requests
from io import BytesIO
import matplotlib.pyplot as plt

# Reading the file
df = pd.read_csv("C:\\Users\\Roy\\Documents\\goodread.csv")
#Reading the first five records

def _removeNonAscii(s):
    return "".join(i for i in s if  ord(i)<128)
# Function for converting into lower case
def make_lower_case(text):
    return text.lower()
# Function for removing stop words
def remove_stop_words(text):
    text = text.split()
    stops = set(stopwords.words("english"))
    text = [w for w in text if not w in stops]
    text = " ".join(text)
    return text
# Function for removing punctuation
def remove_punctuation(text):
    tokenizer = RegexpTokenizer(r'\w+')
    text = tokenizer.tokenize(text)
    text = " ".join(text)
    return text
#Function for removing the html tags
def remove_html(text):
    html_pattern = re.compile('<.*?>')
    return html_pattern.sub(r'', text)
# Applying all the functions in description and storing as a cleaned_desc
df['cleaned_desc'] = df['Desc'].apply(_removeNonAscii)
df['cleaned_desc'] = df.cleaned_desc.apply(func = make_lower_case)
df['cleaned_desc'] = df.cleaned_desc.apply(func = remove_stop_words)
df['cleaned_desc'] = df.cleaned_desc.apply(func=remove_punctuation)
df['cleaned_desc'] = df.cleaned_desc.apply(func=remove_html)

def recommend(title, desc):
    
    # Matching the genre with the dataset and reset the index
    data = df.copy()
    data.reset_index(level = 0, inplace = True) 
  
    # Convert the index into series
    indices = pd.Series(data.index, index = data['title'])
    #Converting the book title into vectors and used bigram
    tf = TfidfVectorizer(analyzer='word', ngram_range=(2, 2), min_df = 1, stop_words='english')
    tfidf_matrix = tf.fit_transform(data['cleaned_desc'])

    desc = [desc]
    tfidf_desc_matrix = tf.transform(desc)

    # Calculating the similarity measures based on Cosine Similarity
    sg = cosine_similarity(tfidf_matrix, tfidf_matrix)
    sg_desc = cosine_similarity(tfidf_matrix, tfidf_desc_matrix)
    
    sig_desc = list(enumerate(sg_desc))
    sig_desc = sorted(sig_desc, key=lambda x: x[1], reverse=True)
    print(sig_desc)
    sig_desc = sig_desc[0:5]
    book_indices = [i[0] for i in sig_desc]
    rec_desc = data[['title']].iloc[book_indices]

    # Get the index corresponding to original_title
       
    idx = indices[title]
    print("The original title is")
    print(idx)

# Get the pairwsie similarity scores 
    sig = list(enumerate(sg[idx]))
# Sort the books
    sig = sorted(sig, key=lambda x: x[1], reverse=True)
# Scores of the 5 most similar books 
    sig = sig[1:6]
# Book indicies
    movie_indices = [i[0] for i in sig]
   
    # Top 5 book recommendation
    rec = data[['title']].iloc[movie_indices]
       
    # It reads the top 5 recommend book url and print the images
    
    print(rec)
    print("According to the description alone here the top picks")
    print(rec_desc)
recommend("Norwegian Wood", "Toru, a quiet and preternaturally serious young college student in Tokyo, is devoted to Naoko, a beautiful and introspective young woman, but their mutual passion is marked by the tragic death of their best friend years before. Toru begins to adapt to campus life and the loneliness and isolation he faces there, but Naoko finds the pressures and responsibilities of life unbearable. As she retreats further into her own world, Toru finds himself reaching out to others and drawn to a fiercely independent and sexually liberated young woman.A magnificent blending of the music, the mood, and the ethos that was the sixties with the story of one college student's romantic coming of age, Norwegian Wood brilliantly recaptures a young man's first, hopeless, and heroic love.")
