      program assignment1
      integer count, size
      real sum, avg, minimum, maximum, num
      logical isFirstNum
c     input the number
      read(*, *, ERR = 500) size
      if (size < 0) then
         goto 500
      endif
c     initializing for input
      isFirstNum = .true.
      count = 0
      do while (count < size)
         read(*, *, ERR = 500) num
         if (isFirstNum) then
            minimum = num
            maximum = num
            isFirstNum = .false.
         else
            minimum = min(minimum, num)
            maximum = max(maximum, num)
         endif
         sum = sum + num
         count = count + 1
      enddo
      if (size == 0) then
         avg = 0
      else
         avg = sum / size;
      endif
c     output the answers
      write(*, 400)'Sum: ',sum
      write(*, 400)'Average: ',avg
      write(*, 400)'Minimum: ',minimum
      write(*, 400)'Maximum: ',maximum
      goto 600
 400  format(a, f30.2)
 500  write(*, *)'ERR'
 600  end