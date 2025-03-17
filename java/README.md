## One Billion Row Challenge

### 1. What Is the One Billion Row Challenge?

### Java
- Solving on [SingleThread](src/main/java/br/com/xmacedo/SingleThreadProcessor.java)
  - 'Single-threaded' took the duration of: 00:08:24.885

- Solving on [Concurrency With Executor Service](src/main/java/br/com/xmacedo/ConcurrencyWithExecutorService.java)
  - Problems: In the first attempt, I faced a problem with Java Heap Space exception, to open the Batch size and create many futures for the processor. So, I need to merge partial results and release the Array line from memory. Which brought even worse results than Single Thread. I will make new modifications to see if I can improve the performance. [WIP]
    - 'Concurrency With Executor' took the duration of: 00:09:44.450
  - 2nd attempt to solving, using Completable Future and I split the file batch size.
    - 'Concurrency With Executor (Different way)' took the duration of: 00:09:22.568

- Solving on [Parallel Stream Processor](src/main/java/br/com/xmacedo/ParallelStreamProcessor.java)
  - Problems: In the first attempt, I faced a problem with Java Heap Space exception, so I changed to read by lines.
    - 'Parellel Stream' took the duration of: 00:04:15.122

- Solving on [Parallel Stream with Flux Reactor](src/main/java/br/com/xmacedo/ParallelStreamWithFluxProcessor.java)
  - 'Parallel Stream With Flux' took the duration of: 00:13:15.272

- Solving on [Memory Mapped IO](src/main/java/br/com/xmacedo/MemoryMappedIOExample.java)
  - 'Memory Mapped IO' took the duration of: 00:21:52.950