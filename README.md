keyword-extraction
==================

(RAKE) Rapid, Automated, Keyword Extraction based Keyword Extraction Tester

This is java implementation of RAKE keyword extraction algorithm


- This isn't optimized for any real world application or solution
- no responsible for any side effect by using this library ( USE WITH YOUR OWN RISK )

2014.06.19
- 형태소 분석기 적용
- STOPTAG 를 기반으로 stopword splitter 로 사용 ${home}/config/rake.properties

2014.06.20
- 관형형 어미를 수반하는 경우는 STOPTAG 에서 제외하고, 원형 복원을 한다. (진행중)
- Negation 의 경우, 는 관련 동사/형용사에 적용하고 원형복원을 시도한다. (진행중)

