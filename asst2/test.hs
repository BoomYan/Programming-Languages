module Main where
import Data.List
import Data.Char
import Data.Bool
import Data.Either
import Control.Exception

deleteN :: [Char] -> Int -> [Char]
deleteN xs n = do    
    let (ys,zs) = splitAt n xs   
    ys ++ (tail zs)

eraseSpace :: [Char] -> Int -> [Char]
eraseSpace input n
    | n >= length input = input
    | isSpace (input !! n) && isSpace (input !! (n - 1)) = eraseSpace (deleteN input n) n
    | otherwise = eraseSpace input (n + 1)

list = [1, 1, 1, 1]

getNth :: (Int) -> (Integer)
getNth n = (getNthList list n) !! n

getNthList :: [Integer] -> (Int) -> [Integer]
getNthList xs n
    | n < 4 = take (n + 1) list
    | length xs == n + 1 = xs
    | otherwise = do let size = length xs
                     getNthList (xs ++ [(xs !! (size-1) + xs !! (size-2))* xs !! (size-3) `div` xs !! (size-4)]) n


getSum :: (Int) -> (Integer)
getSum n = sum (getNthList list n)

getBounds :: (Double) -> [Integer]
getBounds number = do
    let n = getNEqualANumber 0 number
    if ((fromIntegral (getNth n)) == number) then [getNth (n - 1), getNth (n + 1)]
        else [getNth (n - 1), getNth n]

--getFirstNthBiggerOrEqualANumber
getNEqualANumber :: (Int) -> (Double) -> (Int)
getNEqualANumber n number
    | (fromIntegral (getNth n)) >= number = n
    | otherwise = getNEqualANumber (n + 1) number

main = do 
    rawInput <- getLine
    let input = eraseSpace rawInput 1
    if isSpace (input !! 0) || isSpace (input !! (length input - 1)) then do putStrLn"ERR"
    else do
        let command = words input
        if (length command > 2 || length command == 0) then do putStrLn"ERR"
        else do
            if (length command == 1) then
                if (command !! 0 == "QUIT") then return()
                else do putStrLn"ERR"
            else do
            -- length command == 2
                if (command !! 0 == "BOUNDS") then do
                    number <- try(evaluate (read (command !! 1) :: Double)) :: IO (Either SomeException Double)
                    case number of
                        Left ex -> do putStrLn"ERR"
                        Right val -> do let num = read(command !! 1) :: Double
                                        let answer = getBounds num
                                        putStrLn(show (answer !! 0))
                                        putStrLn(show (answer !! 1))
                                        main
                else do    
                    number <- try(evaluate (read (command !! 1) :: Int)) :: IO (Either SomeException Int)
                    case number of 
                        Left ex -> do putStrLn"ERR"
                        Right val -> do
                            let num = read(command !! 1) :: Int
                            case command !! 0 of
                                "NTH" ->  do let answer = getNth num
                                             putStrLn(show answer)
                                             main
                                "SUM" ->  do let answer = getSum num
                                             putStrLn(show answer)
                                             main
                                _ -> putStrLn"ERR"

--for test                           
--main = do
--    input <- getLine
--    putStrLn (eraseSpace input 1)
--    main

