      program assignment1
      implicit none
      integer count, size
      double precision sum, avg, minimum, maximum, num
      logical isFirstNum
      character*100 inputStrA
      character charB
c     input the number
      read(*, '(a)', ERR = 500) inputStrA
      charB = inputStrA(1:1)
      if (charB == '+') then
         goto 500
      endif
      read(inputStrA, *, ERR = 500) size
      if (size < 0) then
         goto 500
      endif
c     initializing for input
      isFirstNum = .true.
      count = 0
      do while (count < size)
         read(*, '(a)', ERR = 500) inputStrA
         read(inputStrA, *, ERR = 500) num
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
      if (sum < 0) then
         goto 500
      endif
      write(*, 400)'Sum: ',sum
      write(*, 400)'Average: ',avg
      if (minimum < 0) then
         goto 500
      endif
      write(*, 400)'Minimum: ',minimum
      write(*, 400)'Maximum: ',maximum
      goto 600
 400  format(a, f50.2)
 500  write(*, 400)'ERR'
 600  end