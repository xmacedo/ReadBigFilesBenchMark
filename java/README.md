## One Billion Row Challenge

### 1. What Is the One Billion Row Challenge?

### Java
- Solving on [SingleThread](src/main/java/br/com/xmacedo/SingleThreadProcessor.java)
  - First Time: 568.038 seconds.
  - Second Time: 578.826 seconds

- Solving on [Concurrency With Executor Service](src/main/java/br/com/xmacedo/ConcurrencyWithExecutorService.java)
  - Problems: In the first attempt, I faced a problem with Java Heap Space exception, to open the Batch size and create many futures for the processor. So, I need to merge partial results and release the Array line from memory. Which brought even worse results than Single Thread. I will make new modifications to see if I can improve the performance. [WIP]
    - First Time: 642.485 seconds.
- > Map reduce, algoritmos paralelos