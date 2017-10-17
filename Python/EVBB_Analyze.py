import csv
import numpy as np
from sklearn.cross_validation import KFold
from sklearn.linear_model import LogisticRegression  
from sklearn.naive_bayes import GaussianNB  
from sklearn.neighbors import KNeighborsClassifier   
from sklearn import svm  
from sklearn.tree import DecisionTreeClassifier  
from sklearn.ensemble import RandomForestClassifier
from itertools import combinations

import matplotlib.pyplot as plt


FILE_NAME_INPUT = r'E:\DevCodes\ProfitSystem\PSAnalyzer\output'

MARK_STR = ['or', 'og', 'ob', 'ok', '^r', '+r', 'sr', 'dr', '<r', 'pr']


def load_csv_data(filename):
    data = []
    for item in csv.reader(open(FILE_NAME_INPUT)):
        data.append([float(field) for field in item])

    data = np.array(data)

    print('Count of records:', len(data))
    return data

def accuracy(test_labels, pred_labels):
    correct = np.sum(test_labels == pred_labels)
    n = len(test_labels)
    return float(correct) / n

#------ Common Classify -------
def testClassify(features, labels, clf):
    kf = KFold(len(features), n_folds = 3, shuffle = True)
    result_set = [(clf.fit(features[train], labels[train]).predict(features[test]), test) for train, test in kf]    
    score = [accuracy(labels[result[1]], result[0]) for result in result_set]    

    #print(score, np.mean(score))
    return np.mean(score)

#-------------------------
#-- Logistic Regression
#-------------------------
def testLR(features, labels):
    clf = LogisticRegression() 
    return testClassify(features, labels, clf)

#-------------------------
#-- naive bayes
#-------------------------
def testGaussianNB(features, labels):
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


#--------------------------------------
#---- LOSS more than -5
#--------------------------------------
def constructFeatures(features, columns):
    return features[:, columns]

def constructLabels(records, roic_th):
    return np.array([int(roic[6] >= roic_th) for roic in records])

def featureColumns(size):
    columns = range(size)
    result_set = []
    for num in range(1, size + 1):
        result_set.extend(list(combinations(columns, num)))
    return result_set

# load origin records type of list
records = load_csv_data(FILE_NAME_INPUT)

def best_evbb():
    # classify loop times
    LOOP_TIMES = 10000
    

    # The variance
    feature_col_list = featureColumns(4)
    roic_list = range(11)

    # index: roic_th index, feature_col
    # value: success probability
    result_set = []
    for roic_th in roic_list: 
        row = []
        #Construct labels
        labels = constructLabels(records, roic_th)
            
        for feature_col in feature_col_list:
            # Construct features
            features = constructFeatures(records, feature_col)
        
            tmp_pro = []
            for i in range(LOOP_TIMES):
                tmp_pro.append(testLR(features, labels))
            
            row.append(np.mean(tmp_pro))

        count = 0.0
        num = 0
        for i in range(len(labels)):
            if labels[i] == 1:
                count += records[i][6]
                num += 1
        max_pro = np.max(row)
        pos = np.where(row == max_pro)[0][0]
        print('Mean return:', count / num, 'of', num, 'in roic:', roic_th, '.Best pro:', np.max(row), feature_col_list[pos])
        
        result_set.append(row)

    # find best
    max_pro = np.max(result_set)
    pos = np.where(result_set == max_pro)
    print('Best setting:', max_pro)
    print('Roic:', roic_list[pos[0][0]])
    print('Columns:', feature_col_list[pos[1][0]])
    

def predict():

    # The variance
    feature_col = (0, 2, 3)
    roic_list = range(2, 11)

    # construct train and test records
    train_records = np.array(list(filter(lambda record : int(record[-1]) < 20170000, records)))
    test_records = np.array(list(filter(lambda record : int(record[-1]) > 20170000, records)))

    print('Train records num:', len(train_records))
    print('Test records num:', len(test_records))
    
    # index: roic_th index, feature_col
    # value: success probability
    for roic_th in roic_list: 
        #Construct labels
        train_features = constructFeatures(train_records, feature_col)
        train_labels = constructLabels(train_records, roic_th)

        # Train LR Classify
        clf = LogisticRegression()
        fiter = clf.fit(train_features, train_labels)

        # Test
        test_features = constructFeatures(test_records, feature_col)
        test_labels = constructLabels(test_records, roic_th)
        
        result_set = (fiter.predict(test_features), range(len(test_features)))
        score = accuracy(test_labels[result_set[1]], result_set[0])
        print('Satisified count:',np.sum(test_labels == 1))
        print('Roic:', roic_th, 'Probability:', score)
        print()


predict()


'''
tarin_features = np.array(list(filter(lambda fields : fields[6] > 0 and fields[6] < 10, orign_features)))
test_features = np.array(list(filter(lambda fields : fields[6] <= 0 or fields[6] >= 10, orign_features)))
'''

'''
tarin_features = orign_features[:-10]
test_features = orign_features[-10:]

#test_features = np.array([test_features])


tarin_features, train_lables = constructLossData(tarin_features)
test_features, test_lables = constructLossData(test_features)


clf = LogisticRegression()

fiter = clf.fit(tarin_features, train_lables)

result_set = (fiter.predict(test_features), range(len(test_features)))
score = accuracy(test_lables[result_set[1]], result_set[0])
print(score, np.mean(score)) 



print('LogisticRegression: \r')  
testLR(features, labels)  
      
print('GaussianNB: \r')  
testGaussianNB(features, labels)  
      
print('KNN: \r')  
testKNN(features, labels)  
      
print('SVM: \r')  
testSVM(features, labels)  
      
print('Decision Tree: \r')  
testDecisionTree(features, labels)  
      
print('Random Forest: \r')  
testRandomForest(features, labels)


data_set = orign_features[:, 0]
data_set = np.column_stack((data_set, orign_features[:, [6]]))

for i in range(len(data_set)):
    plt.plot(data_set[i][0], data_set[i][1], MARK_STR[labels[i]])

#plt.show()
'''
