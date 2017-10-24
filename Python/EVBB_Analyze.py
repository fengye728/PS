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
    new_tab = np.column_stack((test_labels, pred_labels))

    #n = np.sum(new_tab[:, 0] == 1) # 应入场次数
    n = np.sum(new_tab[:, 1] == 1)  # 入场次数
    
    correct = len(list(filter(lambda x : x[0] == x[1] and x[0] == 1, new_tab)))

    if n == 0 or correct == 0:
        return 0
    
    return float(correct) / n

#------ Common Classify -------
def testClassify(features, labels, clf):
    kf = KFold(len(features), n_folds = 2, shuffle = True)
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
    return testClassify(features, labels, clf)

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
    return testClassify(features, labels, clf)
    
#------------------------------------------------------------------------------  
#--- 决策树  
#------------------------------------------------------------------------------  
def testDecisionTree(features, labels):   
    clf = DecisionTreeClassifier()  
    return testClassify(features, labels, clf)

#------------------------------------------------------------------------------  
#--- 随机森林  
#------------------------------------------------------------------------------  
def testRandomForest(features, labels):  
    clf = RandomForestClassifier()  
    return testClassify(features, labels, clf)


#--------------------------------------
#---- LOSS more than -5
#--------------------------------------
def constructFeatures(features, columns):
    return features[:, columns]

def constructLabels(records, roic_th):
    return np.array([int(roic[6] >= roic_th) for roic in records])

def constructHighLowLabels(records, gain_roic_th, loss_roic_th):
    return np.array([ int(roic[4] > loss_roic_th and roic[5]>= gain_roic_th) for roic in records])

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
    LOOP_TIMES = 1000
    

    # The variance
    feature_col_list = featureColumns(3)
    roic_list = range(0, 11)

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
                tmp_pro.append(testSVM(features, labels))
            
            row.append(np.mean(tmp_pro))

        count = 0.0
        num = 0
        for i in range(len(labels)):
            if labels[i] == 1:
                count += records[i][6]
                num += 1
        max_pro = np.max(row)
        pos = np.where(row == max_pro)[0][0]
        print('Mean return:', count / num, 'of', num, 'in roic:', roic_th, '.Best pro:', np.max(row), 'Difference:', np.max(row) - np.min(row),feature_col_list[pos])
        
        result_set.append(row)

    # find best
    max_pro = np.max(result_set)
    pos = np.where(result_set == max_pro)
    print()
    print('Best setting:', max_pro)
    print('Roic:', roic_list[pos[0][0]])
    print('Columns:', feature_col_list[pos[1][0]])


def get_profit(high, low):
    return high - low

def predict():

    # The variance
    feature_col = (0, 1, 2, 3)
    stop_roic_list = range(-10, 0)
    gain_roic_list = range(5, 20)
    

    # construct train and test records
    train_records = np.array(list(filter(lambda record : int(record[-1]) < 20170000, records)))
    test_records = np.array(list(filter(lambda record : int(record[-1]) > 20170000, records)))

    print('Train records num:', len(train_records))
    print('Test records num:', len(test_records))
    print()

    total_list = []
    # index: roic_th index, feature_col
    # value: success probability
    for loss_th in stop_roic_list:

        for gain_th in gain_roic_list:
        
            #Construct labels
            train_features = constructFeatures(train_records, feature_col)
            train_labels = constructHighLowLabels(train_records, gain_th, loss_th)

            # Train Classify
            clf = GaussianNB()
            fiter = clf.fit(train_features, train_labels)

            # Test
            test_features = constructFeatures(test_records, feature_col)
            test_labels = constructHighLowLabels(test_records, gain_th, loss_th)
        
            result_set = fiter.predict(test_features)
            score = accuracy(test_labels, result_set)

            entry_index_list = list(filter(lambda item : item == 1, result_set))
            total_list.append([gain_th, loss_th, score, len(entry_index_list), score * gain_th + (1 - score) * loss_th])
            
            #print('Gain roic:', gain_th, 'Lost roic:', loss_th, 'Probability:', score, 'Total:', score * gain_th + (1 - score) * loss_th)
            #print('Satisified count:',np.sum(test_labels == 1))
            
        
           # print('entry times: ', len(entry_index_list))
            #print('Mean profit:', np.mean(test_records[success_index_list][:, 6]))
            #print()
            
    ordered_list = sorted(total_list, key = lambda item : item[-1], reverse = True)
    for item in ordered_list:
        print(item)

def find_by_min():
    min_roic_th_list = range(-10,0)

    for roic_th in min_roic_th_list:
        fit_records = np.array(list( filter(lambda record : record[4] >= roic_th, records) ))
        print('Roic in lose rate:', roic_th, 'Mean roic:', fit_records[:, 6].sum() / len(fit_records), 'of', len(fit_records))


for record in records:
    mark = int(float(record[6]) > 0)

    plt.plot(record[2], record[0], MARK_STR[mark])
#plt.show()

#find_by_min()
predict()
#best_evbb()
