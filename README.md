# JIPOC
Job Interview Proof Of Concept

[![Video](https://j.gifs.com/D1NvJ5.gif)](https://www.youtube.com/watch?v=yd8RVbCVXJg)

## Main Challenges
1. Creating realtime chat system with End To End Encryption (like telegram or whatsup).
2. Key exchange without expose to MItM attack.
3. Completely anonymous - Authentication without identification (unlike telegram or whatsup).

## Application flow
1. First user (initiator) starts the chat - the app generates `first key` (8 random chars)
2. Second user joins the chat - the app generates for him `second key` (8 random chars)
3. They exchange the keys physically (not through network), a combination of those keys (all 16 chars) is later used as the `Private Key` to encrypt and decrypt messages at each end.
4. Starting the chat (initiator) create [SHA256](https://en.wikipedia.org/wiki/SHA-2) hash which is made from the combination of both keys (16 chars together).
Then the hash is encoded to [Base64](https://en.wikipedia.org/wiki/Base64) and sends a `HTTP Request` for creating a new chat.
The chat created by that charsequence, i.e. `Base64(Sha256(key1+key2))`.
When the chat is created it generates a random `IV` [Initialization Vector](https://en.wikipedia.org/wiki/Initialization_vector) by all best practices of the `AES` [Advanced Encryption Standard](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard).
5. Joining the chat uses the same `SHA256`, which is possible since it also has both keys. And then sends `HTTP Request` to join the chat, if succeeded it receives the `IV` for that chat.

**Note** `SHA256` cannot be reversed and since it's created from 16 random chars, it's immune for brute force attack as well. 

6. After both users open the chat, they both have the relevant `IV` and the `Private Key`. Each one now can send a message.
7. When users close the chat all the data is destroyed.
8. Chat communication uses `EE2E` [End To End Encryption](https://en.wikipedia.org/wiki/End-to-end_encryption) based on `CBC` [Cypher Block Chaining](Cipher_Block_Chaining_.28CBC.29) algorithm with `PKCS#7` [Public Key Cipher Standard](https://en.wikipedia.org/wiki/PKCS) padding for symmetric encryption.
9. To make the chat look nice, the messages are displayed in bubbles (idea from telegram and whatsup) separated by color.
10. For realtime communication I used `Firebase` cloud platform.

## Code design

#### One single `Activity` class, because it is a simple single page application.
- [MainActivity.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/MainActivity.java)

#### package `utils` contains different utility classes, that I created for easier usage.

- [Crypto.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/utils/Crypto.java) - for cryptographic usage, such as `SHA256` and `AES` encryption and decryption.

- [HttpRequest.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/utils/HttpRequest.java) - for building HTTP Request in an easy and straight forward way

- [KeyManager.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/utils/KeyManager.java) - for easy key management

- [RestManager.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/utils/RestManager.java) - for managing the communication with remote RESTful API 

- [FBHelper.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/utils/FBHelper.java) - helping to manage the data structures and the keys needed for the Firebase

- [Alert.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/utils/Alert.java) - simple shortcuts for toast and dialog popups 

#### package `models` currently contains only the `Message` class, but it's the best practice to separate model classes for easier code maintaince. 

- [Message.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/models/Message.java) - data struct for a single message object.

#### package `adapters` currently contains one adapter `MessagesAdapter`, but I always separate adapters from all the other classes for easier maintaince.
- [MessagesAdapter.java](https://github.com/nikitaKurtin/JIPOC/blob/master/android/JIPOC/app/src/main/java/kurtin/nikita/jipoc/adapters/MessagesAdapter.java) - adapts between the list of messages and the views displayed in the `RecyclerView`

