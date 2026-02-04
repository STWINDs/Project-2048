# Conditionals:

go语言与其他语言不同，if必须将左大括号放在与条件相同的行上，而不是另起一行。  
条件不加括号，常用操作符：  
* ==  equal to
* !=  not equal to
* <   less than
*  \>  greater than
* <= less than or equal to
*  \>= greater than or equal to

'initial' section:  Statement形如

`if INITIAL_STATEMENT; CONDITION {
}
`

e.g:

`
if length := getLength(email); length < 1 {
fmt.Println("Email is invalid")
}
`
此时的initial不会出现在函数的父作用域中

switch语句：

```
 switch variable{
    case choice1:
         statement1
    case choice2:
         statement2
    default:
         statement3
    }
`````



e.g:
```
func getCreator(os string) string {
    var creator string
    switch os {
    case "linux":
        creator = "Linus Torvalds"
    case "windows":
        creator = "Bill Gates"
    case "mac":
        creator = "A Steve"
    default:
        creator = "Unknown"
    }
    return creator
}
```

# Function:

* Go 中的函数可以接受零个或多个参数。  
为了让代码更易读，变量类型会放在变量名之后。 且返回值is known as the **"function signature"**
当多个参数属于相同类型，并且在函数签名中相邻时，类型只需在最后一个参数之后声明

* Go-Style Syntax  
Go's declarations are clear, you just read them left to right, just like you would in English.

* Passing Variables by Value:  
变量是按值传递的,意味着当变量被传递到函数时，该函数会获得该变量的副本。该函数无法改变调用者的原始数据。

* Ignoring Return Values  
我们可以选择性的将函数的返回值赋给变量，如果某一返回值并不被需要，我们可以用_来作为空白标识符标注：        
e.g:
```
func getPoint() (x int, y int) {
    return 3, 4
}
// ignore y value
x, _ := getPoint()
```

* Named Return Values:命名返回值   
可以在函数Signature处提前预设返回值的变量名，e.g  :
```
func getCoords() (x, y int) {
	// x and y are initialized with zero values

	return // automatically returns x and y
}
```
什么时候使用named returns?   When there are many values being returned   
BTW,即使函数有named return，我们也可以返回Explict returns(显式返回)，形如某一变量/某一常数

* Early returns:    
提前返回  Guard clauses provide a linear approach to logic trees,   
guard clause: An early return from a function when a given condition is met   e.g:   
````
func divide(dividend, divisor int) (int, error) {
	if divisor == 0 {
		return 0, errors.New("can't divide by zero")
	}
	return dividend/divisor, nil
}
````

* Functions As Values    
我们可以将一个函数作为形参传入另一个函数中，注意：作为形参的函数无需传入任何值，只是给了新的函数一个调用接口，e.g:    
```
func aggregate(a, b, c int, arithmetic func(int, int) int) int {
     firstResult := arithmetic(a, b)
     secondResult := arithmetic(firstResult, c)
     return secondResult
}
func main() {
	sum := aggregate(2, 3, 4, add)
	// sum is 9
	product := aggregate(2, 3, 4, mul)
	// product is 24
}
```

*  Anonymous Functions:  
匿名函数顾名思义，没有名称。它们适用于定义仅使用一次的函数或创建快速闭包的情况。
exercise:
```
package main

import "fmt"

func printReports(intro, body, outro string) {
	printCostReport(func(a string) int{return len(a)*2},intro)
	printCostReport(func(a string) int{return len(a)*3},body)
	printCostReport(func(a string) int{return len(a)*4},outro)
}

// don't touch below this line

func main() {
	printReports(
		"Welcome to the Hotel California",
		"Such a lovely place",
		"Plenty of room at the Hotel California",
	)
}

func printCostReport(costCalculator func(string) int, message string) {
	cost := costCalculator(message)
	fmt.Printf(`Message: "%s" Cost: %v cents`, message, cost)
	fmt.Println()
}

```



