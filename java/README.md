## One Billion Row Challenge

### 1. What Is the One Billion Row Challenge?

### Java
- Solving on [SingleThread](src/main/java/br/com/xmacedo/SingleThreadProcessor.java)
  - First Time: 568.038 seconds.
  - Second Time: 578.826 seconds

- Solving on [Concurrency With Executor Service](src/main/java/br/com/xmacedo/ConcurrencyWithExecutorService.java)
  - Problems: In the first attempt, I faced a problem with Java Heap Space exception, to open the Batch size and create many futures for the processor. So, I need to merge partial results and release the Array line from memory. Which brought even worse results than Single Thread. I will make new modifications to see if I can improve the performance. [WIP]
    - First Time: 562.285 seconds.
  - 2nd attempt to solving, I split 
    - First Time: 550.309 seconds.

- Solving on [Parallel Stream Processor](src/main/java/br/com/xmacedo/ParallelStreamProcessor.java)
  - Problems: In the first attempt, I faced a problem with Java Heap Space exception, so I changed to read by lines.
    - First Time: 235.426 seconds.

- Solving on [Parallel Stream with Flux Reactor](src/main/java/br/com/xmacedo/ParallelStreamWithFluxProcessor.java)
  - First Time: 789.125 seconds.