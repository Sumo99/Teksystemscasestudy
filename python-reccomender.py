import pandas as pd
import numpy as np
from nltk.corpus import stopwords
from sklearn.metrics.pairwise import cosine_similarity
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
from flask import Flask, redirect, url_for, request
from sqlalchemy import create_engine 
# Reading the file

cnx = create_engine("mysql+pymysql://library:pass@localhost:3306/library").connect()

df = pd.read_sql_table("mroonga_books", cnx)
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
df['cleaned_desc'] = df['description'].apply(_removeNonAscii)
df['cleaned_desc'] = df.cleaned_desc.apply(func = make_lower_case)
df['cleaned_desc'] = df.cleaned_desc.apply(func = remove_stop_words)
df['cleaned_desc'] = df.cleaned_desc.apply(func=remove_punctuation)
df['cleaned_desc'] = df.cleaned_desc.apply(func=remove_html)

def recommend(desc):
    
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
    sg_desc = cosine_similarity(tfidf_matrix, tfidf_desc_matrix)
    
    sig_desc = list(enumerate(sg_desc))
    sig_desc = sorted(sig_desc, key=lambda x: x[1], reverse=True)
    sig_desc = sig_desc[0:5]
    book_indices = [i[0] for i in sig_desc]
    rec_desc = data[['title']].iloc[book_indices]

    # Get the index corresponding to original_title
       
    # idx = indices[title]
    
    
    print("According to the description alone here the top picks")
    rec_desc = rec_desc["title"]
    return rec_desc.to_list()

app = Flask(__name__)

@app.route('/book/<content>')
def echo(content):
    book_contents = ""
    for item in recommend(content):
        book_contents = book_contents + item
        book_contents = book_contents + "<br>"
    return book_contents

@app.route('/book/')
def empty_description():
    return ""

if __name__ == "__main__":
    app.run()

