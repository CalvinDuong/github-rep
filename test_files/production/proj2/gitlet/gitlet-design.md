# Gitlet Design Document

**Name**: Calvin Duong and Vivi Thai

## Classes and Data Structures

### Commit

#### Instance Variables
* Message - contains commit message
* Timestamp - time at which commit was created
* Parent - the parent commit of a commit object
* File - the file we are plan to store in this commit 
  * The file we are planning to store the commit will be in the blobs folder with each respective blob file 
* Parameters - message, parent, file 


# INIT()
* We are planning to create Folders for all the different stages 
* We are planning to serialize the blobs so we can store it in a file in the folder
* We need to create a head variable that is of Commit Type to point to the current commit 
* 



## Algorithms


## Persistence

### HEAD 
* A Head points to the current working directory
* Typically the head is the latest commit but not always the case 
* Use a doubly linked list where the head points to the current node 
