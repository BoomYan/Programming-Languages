module Main where
import Data.List
import Data.Char
import Control.Exception

splitOn :: Eq a => [a] -> [a] -> [[a]]
splitOn delim xs = loop xs
    where loop [] = [[]]
          loop xs | delim `isPrefixOf` xs = [] : splitOn delim (drop len xs)
          loop (x:xs) = let (y:ys) = splitOn delim xs
                         in (x:y) : ys
          len = length delim

list = [1, 1, 1, 1]

--getNth :: (Int) -> (Integer)
--getNth n
--    | n < length list = list !! n
--    | otherwise = (getNth(n-1) + getNth(n-2))* getNth(n-3) `div` getNth(n-4)

getNth :: (Int) -> (Integer)
getNth n = do (getNthList list n) !! n

getNthList :: [Integer] -> (Int) -> [Integer]
getNthList xs n
    | length xs == n + 1 = xs
    | otherwise = do let size = length xs
                     getNthList (xs ++ [(xs !! (size-1) + xs !! (size-2))* xs !! (size-3) `div` xs !! (size-4)]) n


getSum :: (Int) -> (Integer)
getSum n = sum (getNthList list n)
{- getLowerBoundIndex :: (Int) -> (Int) -> [Int]
    getLowerBoundIndex number index = do
                             let 46904948 -}

main = do 
    input <- getLine
    let command = splitOn " " input
    if (length command > 2 || length command == 0) then do putStrLn"ERR"
    else do
        if (length command == 1) then
            if (command !! 0 == "QUIT") then return()
            else do putStrLn"ERR"
        else do
        -- length command == 2
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
                        "BOUNDS" -> do let answer = getBoundIndexes num
                                         putStrLn(show answer !! 0)
                                         putStrLn(show answer !! 1)
                                         main
                        _ -> putStrLn"ERR"

