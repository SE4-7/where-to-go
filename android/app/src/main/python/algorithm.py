from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
import numpy as np
import pandas as pd

def getKeyword(choice):
    data = ''
    features = ["한식 쌀밥 ", "한식 고기 닭 튀김", "양식 빵 밀가루 채소", "중식 면 밀가루", "중식 면 해산물", "양식 빵 치즈 밀가루", "일식 해산물 물고기",
                "한식 밥 돼지 고기", "양식 고기 소 레스토랑", "분식 떡"]
    for idx in choice:
        data += ' ' + features[idx]
    tfidf = TfidfVectorizer(analyzer="word")
    tfidf_matrix = tfidf.fit_transform([data])
    for idf in tfidf_matrix.toarray():
        return tfidf.get_feature_names()[np.where(idf == max(idf))[0][0]]
