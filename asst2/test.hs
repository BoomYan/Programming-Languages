module Main where
import Data.List
import Data.Char

deleteN :: [Char] -> Int -> [Char]
deleteN xs n = do    
    let (ys,zs) = splitAt n xs   
    ys ++ (tail zs)

eraseSpace :: [Char] -> Int -> [Char]
eraseSpace input n
    | n >= length input = input
    | isSpace (input !! n) && isSpace (input !! (n - 1)) = eraseSpace (deleteN input n) n
    | otherwise = eraseSpace input (n + 1)

getNth :: Int -> Integer
getNth targetIndex = getNthHelper 1 1 1 1 3 targetIndex

--new getNth
getNthHelper :: Integer -> Integer -> Integer -> Integer -> Int -> Int -> Integer
getNthHelper a b c d n targetIndex
    | targetIndex <= 3 = 1
    | n == targetIndex = d
    | otherwise = getNthHelper b c d ((d + c) * b `div` a) (n + 1) targetIndex

getSum :: Int -> Integer
getSum targetIndex = getSumHelper 1 1 1 1 3 targetIndex 4

getSumHelper :: Integer -> Integer -> Integer -> Integer -> Int -> Int -> Integer -> Integer
getSumHelper a b c d n targetIndex currSum
    | targetIndex <= 3 = fromIntegral (targetIndex + 1)
    | n == targetIndex = currSum
    | otherwise = do
        let e = ((d + c) * b `div` a)
        getSumHelper b c d e (n + 1) targetIndex (currSum + e)

getBounds :: Double -> [Integer]
getBounds number = do
    let n = getNEqualANumber 1 1 1 1 3 number
    if ((fromIntegral (getNth n)) == number) then [getNth (n - 1), getNth (n + 1)]
    else [getNth (n - 1), getNth n]

--getFirstNthBiggerOrEqualANumber
getNEqualANumber :: Integer -> Integer -> Integer -> Integer -> Int -> Double -> Int
getNEqualANumber a b c d n targetNum
    | (fromIntegral d) >= targetNum = n
    | otherwise = getNEqualANumber b c d ((d + c) * b `div` a) (n + 1) targetNum

parseDouble :: [Char] -> Double
parseDouble xs = do
    let args = reads xs :: [(Double, String)]
    case args :: [(Double, String)] of 
        [(num, "")] -> num
        [(num, ".")] -> num
        otherwise -> -1

parseInt :: [Char] -> Int
parseInt xs = do
    let args = reads xs :: [(Int, String)]
    case args :: [(Int, String)] of 
        [(num, "")] -> num
        otherwise -> -1

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
                    let num = parseDouble (command !! 1)
                    if (num <= 1) then do putStrLn"ERR"
                        else do
                            let answer = getBounds num
                            putStrLn(show (answer !! 0))
                            putStrLn(show (answer !! 1))
                            main
                else do
                    let num = parseInt (command !! 1)
                    if (num < 0) then do putStrLn"ERR"
                        else do
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

