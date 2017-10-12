import csv
import numpy as np
from sklearn.cross_validation import KFold  
from sklearn.linear_model import LogisticRegression  
from sklearn.naive_bayes import GaussianNB  
from sklearn.neighbors import KNeighborsClassifier   
from sklearn import svm  
from sklearn.tree import DecisionTreeClassifier  
from sklearn.ensemble import RandomForestClassifier 

import matplotlib.pyplot as plt


FILE_NAME_INPUT = r'E:\DevCodes\ProfitSystem\PSAnalyzer\output'


def load_csv_data(filename):
    data = []
    labels = []
    for item in csv.reader(open(FILE_NAME_INPUT)):
        data.append([float(field) for field in item[:-1]])
        labels.append(int(float(item[-1]) > 0))

    data = np.array(data)
    labels = np.array(labels)

    return data,labels

def accuracy(test_labels, pred_labels):
    correct = np.sum(test_labels == pred_labels)
    n = len(test_labels)
    return float(correct) / n

#------ Common Classify -------
def testClassify(features, labels, clf):
    kf = KFold(len(features), n_folds = 3, shuffle = True)
    result_set = [(clf.fit(features[train], labels[train]).predict(features[test]), test) for train, test in kf]    
    score = [accuracy(labels[result[1]], result[0]) for result in result_set]    
    print(score)  

#-------------------------
#-- Logistic Regression
#-------------------------
def testLR(features, labels):
    clf = LogisticRegression() 
    testClassify(features, labels, clf)

#-------------------------
#-- naive bayes
#-------------------------
def testNaiveBayes(features, labels):
    clf = GaussianNB() 
    testClassify(features, labels, clf)

#------------------------------------------------------------------------------  
#K最近邻  
#------------------------------------------------------------------------------  
def testKNN(features, labels):  
    clf = KNeighborsClassifier(n_neighbors=5)   
    testClassify(features, labels, clf)

#-------------------------
#-- SVM
#-------------------------
def testSVM(features, labels):
    clf = svm.SVC()
    testClassify(features, labels, clf)
    
#------------------------------------------------------------------------------  
#--- 决策树  
#------------------------------------------------------------------------------  
def testDecisionTree(features, labels):   
    clf = DecisionTreeClassifier()  
    testClassify(features, labels, clf)

#------------------------------------------------------------------------------  
#--- 随机森林  
#------------------------------------------------------------------------------  
def testRandomForest(features, labels):  
    clf = RandomForestClassifier()  
    testClassify(features, labels, clf)

features, labels = load_csv_data(FILE_NAME_INPUT)

print('LogisticRegression: \r')  
testLR(features, labels)  
      
print('GaussianNB: \r')  
testNaiveBayes(features, labels)  
      
print('KNN: \r')  
testKNN(features, labels)  
      
print('SVM: \r')  
testSVM(features, labels)  
      
print('Decision Tree: \r')  
testDecisionTree(features, labels)  
      
print('Random Forest: \r')  
testRandomForest(features, labels)  

'''
print('Step 1: Loading data...')
data_set = []
for item in csv.reader(open(FILE_NAME_INPUT)):
    data_set.append([float(item[1]), float(item[4]) * 100])

numSamples = len(data_set)

MARK_STR = ['or', 'ob', 'og', 'ok', '^r', '+r', 'sr', 'dr', '<r', 'pr']

numZero = 0
numNeg = 0
numTen = 0
numTwf = 0
for i in range(numSamples):
    if(data_set[i][1] >= 20):
        mark = MARK_STR[0]
        numTwf += 1
    elif(data_set[i][1] >= 10):
        mark = MARK_STR[1]
        numTen += 1
    elif(data_set[i][1] >= 0):
        mark = MARK_STR[2]
        numZero += 1
    else:
        mark = MARK_STR[3]
        numNeg += 1        
    
    plt.plot(data_set[i][0], data_set[i][1], mark)

print('Count of 20%:', numTwf)
print('Count of 10%:', numTen)
print('Count Postive:', numTwf + numZero + numTen)
print('Count Negative:', numNeg)

plt.show()
'''


'''
for k in range(1, 2):
    clf = KMeans(n_clusters = k)
    s = clf.fit(data_set)
    
    numSamples = len(data_set)
    centroids = clf.labels_

    print(centroids, type(centroids))
    print(clf.inertia_)
    
    
    #画出所有样例点 属于同一分类的绘制同样的颜色
    for i in range(numSamples):
        #markIndex = int(clusterAssment[i, 0])
        plt.plot(data_set[i][0], data_set[i][1], MARK_STR[clf.labels_[i]]) #mark[markIndex])
        mark = ['Dr', 'Db', 'Dg', 'Dk', '^b', '+b', 'sb', 'db', '<b', 'pb']
        # 画出质点，用特殊图型
        centroids =  clf.cluster_centers_
        for i in range(k):
            plt.plot(centroids[i][0], centroids[i][1], mark[i], markersize = 12)
            #print centroids[i, 0], centroids[i, 1]
    plt.show()
'''
