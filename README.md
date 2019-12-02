## Learning Source

This is a small project that I can one day use to track the learning sources that I want to use for my studies. The idea is to improve it so I can prioritise things to study, track what has been done and what is remainig, as well as create to-do lists bound to any learning source.

The main motivation to create this project was honestly to run some experiments on DynamoDB and probably get a by-product that can be useful for me. If it become useful enough for me I will probably rely on any other cheaper database instead of DDB, however I intend to keep the source code for reference purpose.

### Data model

Table: LearningSource
Partition key: hash (md5 generated from link + '/' + title)
Sort key: registryDate

Fields: description, link

---

Global secondary index
Partition key: uuid (this is useless, but I am going to use it for my experiments)
