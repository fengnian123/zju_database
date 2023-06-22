import entities.Book;
import entities.Borrow;
import entities.Card;
import queries.*;
import utils.ConnectConfig;
import utils.DatabaseConnector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // parse connection config from "resources/application.yaml"
            ConnectConfig conf = new ConnectConfig();
            log.info("Success to parse connect config. " + conf.toString());
            // connect to database
            DatabaseConnector connector = new DatabaseConnector(conf);
            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            /* do somethings */
            LibraryManagementSystem library = new LibraryManagementSystemImpl(connector);
            System.out.println("欢迎使用本数据库管理系统");
            while(true){
                System.out.println("请选择你要进行的操作（输入数字1~12）：");
                System.out.println("1.向图书库中注册(添加)一本新书");
                System.out.println("2.为图书库中的某一本书增加库存");
                System.out.println("3.批量入库图书");
                System.out.println("4.从图书库中删除一本书");
                System.out.println("5.修改已入库图书的基本信息");
                System.out.println("6.图书查询");
                System.out.println("7.借书模块");
                System.out.println("8.借书记录查询");
                System.out.println("9.借书证注册");
                System.out.println("10.删除借书证");
                System.out.println("11.借书证查询");
                System.out.println("12.还书模块");
                System.out.println("13.退出本系统");
                System.out.println("14.清空数据库");
                Scanner scanner = new Scanner(System.in);
                String temp=scanner.nextLine();
                int a=Integer.parseInt(temp);
                if(a==1){
                    System.out.println("1.向图书库中注册(添加)一本新书");
                    System.out.println("输入图书类别（category）:");
                    String category = scanner.nextLine();
                    System.out.println("输入图书题目（title）:");
                    String title = scanner.nextLine();
                    System.out.println("输入图书出版社（press）:");
                    String press = scanner.nextLine();
                    System.out.println("输入图书出版年份（publishYear）:");
                    String temp1=scanner.nextLine();
                    int publishYear = Integer.parseInt(temp1);
                    System.out.println("输入图书作者（author）:");
                    String author = scanner.nextLine();
                    System.out.println("输入图书价格（price）:");
                    String temp3 = scanner.nextLine();
                    Double price = Double.parseDouble(temp3);
                    System.out.println("输入图书库存（stock）:");
                    String temp2=scanner.nextLine();
                    int stock = Integer.parseInt(temp2);
                    Book book = new Book(
                            category,
                            title,
                            press,
                            publishYear,
                            author,
                            price,
                            stock
                    );
                    ApiResult result = library.storeBook(book);
                    if(result.ok){
                        System.out.println("插入成功");
                    }
                    else{
                        System.out.println("插入失败，请检查该图书是否已经存在");
                    }
                }
                else if(a==2){
                    System.out.println("2.为图书库中的某一本书增加库存");
                    System.out.println("请输入图书编号：");
                    String temp1=scanner.nextLine();
                    int book_id = Integer.parseInt(temp1);
                    System.out.println("请输入改变的图书数量：");
                    String temp2=scanner.nextLine();
                    int increase = Integer.parseInt(temp2);
                    ApiResult result = library.incBookStock(book_id,increase);
                    if(result.ok){
                        System.out.println("修改成功");
                    }
                    else{
                        System.out.println("修改失败，请检查修改后存量是否小于0");
                    }
                }
                else if(a==3){
                    System.out.println("3.批量入库图书");
                    List<Book> books = new ArrayList<>();
                    String filePath = "D:\\study\\database\\w5\\librarymanagementsystem-master\\src\\main\\java\\book_data.txt";
                    File file = new File(filePath);
                    System.out.println("文件名称:" + file.getName());
                    System.out.println("文件是否存在：" + file.exists());
                    System.out.println("文件大小：" + file.length() + "B");
                    Scanner scanner2 = new Scanner(file);
                    while(scanner2.hasNextLine()){
                        //根据用户输入的信息创建字符串
                            String category=scanner2.nextLine();
                            String title = scanner2.nextLine();
                            String press = scanner2.nextLine();
                            String temp2=scanner2.nextLine();
                            int publishYear = Integer.parseInt(temp2);
                            String author = scanner2.nextLine();
                            String temp5 = scanner2.nextLine();
                            Double price = Double.parseDouble(temp5);
                            String temp3 = scanner2.nextLine();
                            int stock = Integer.parseInt(temp3);
                            Book book = new Book(
                                    category,
                                    title,
                                    press,
                                    publishYear,
                                    author,
                                    price,
                                    stock
                            );
                            books.add(book);
                    }
                    scanner2.close();        //关闭流
                    ApiResult result = library.storeBook(books);
                    if(result.ok){
                        System.out.println("入库成功");
                    }
                    else{
                        System.out.println("入库失败，请检查图书是否已经存在");
                    }
                }
                else if(a==4){
                    System.out.println("4.从图书库中删除一本书");
                    System.out.println("请输入图书编号：");
                    String temp1 = scanner.nextLine();
                    int book_id = Integer.parseInt(temp1);
                    ApiResult result = library.removeBook(book_id);
                    if(result.ok){
                        System.out.println("删除成功");
                    }
                    else{
                        System.out.println("删除失败");
                    }
                }
                else if(a==5){
                    System.out.println("5.修改已入库图书的基本信息");
                    System.out.println("请输入图书编号：");
                    String temp1 = scanner.nextLine();
                    int book_id = Integer.parseInt(temp1);
                    System.out.println("输入图书类别（category）:");
                    String category = scanner.nextLine();
                    System.out.println("输入图书题目（title）:");
                    String title = scanner.nextLine();
                    System.out.println("输入图书出版社（press）:");
                    String press = scanner.nextLine();
                    System.out.println("输入图书出版年份（publishYear）:");
                    String temp2 = scanner.nextLine();
                    int publishYear = Integer.parseInt(temp2);
                    System.out.println("输入图书作者（author）:");
                    String author = scanner.nextLine();
                    System.out.println("输入图书价格（price）:");
                    String temp5 = scanner.nextLine();
                    Double price = Double.parseDouble(temp5);
                    System.out.println("输入图书库存（stock）:");
                    String temp3 = scanner.nextLine();
                    int stock = Integer.parseInt(temp3);
                    Book book = new Book(
                            category,
                            title,
                            press,
                            publishYear,
                            author,
                            price,
                            stock
                    );
                    book.setBookId(book_id);
                    ApiResult result = library.modifyBookInfo(book);
                    if(result.ok){
                        System.out.println("修改成功");
                    }
                    else{
                        System.out.println("修改失败，请检查图书是否存在");
                    }
                }
                else if(a==6){
                    BookQueryConditions condition=new BookQueryConditions();
                    System.out.println("6.图书查询(只能选一种方式查询)");
                    System.out.println("是否使用图书类别排序(如果不用该类型的查询，请输入-1):");
                    String category = scanner.nextLine();
                    if(!category.equals("-1")){
                        condition.setCategory(category);
                        condition.setSortBy(Book.SortColumn.CATEGORY);
                    }
                    System.out.println("输入图书题目(如果不用该类型的查询，请输入-1):");
                    String title = scanner.nextLine();
                    if(!title.equals("-1")){
                        condition.setTitle(title);
                        condition.setSortBy(Book.SortColumn.TITLE);
                    }
                    System.out.println("输入图书出版社(如果不用该类型的查询，请输入-1):");
                    String press = scanner.nextLine();
                    if(!press.equals("-1")){
                        condition.setPress(press);
                        condition.setSortBy(Book.SortColumn.PRESS);
                    }
                    System.out.println("输入图书最小出版年份(如果不用该类型的查询，请输入-1):");
                    String temp1 = scanner.nextLine();
                    int minpublishYear = Integer.parseInt(temp1);
                    if(minpublishYear!=-1){
                        condition.setMinPublishYear(minpublishYear);
                        condition.setSortBy(Book.SortColumn.PUBLISH_YEAR);
                    }
                    System.out.println("输入图书最大出版年份(如果不用该类型的查询，请输入-1):");
                    String temp2 = scanner.nextLine();
                    int maxpublishYear = Integer.parseInt(temp2);
                    if(maxpublishYear!=-1){
                        condition.setMaxPublishYear(maxpublishYear);
                        condition.setSortBy(Book.SortColumn.PUBLISH_YEAR);
                    }
                    System.out.println("输入图书作者(如果不用该类型的查询，请输入-1):");
                    String author = scanner.nextLine();
                    if(!author.equals("-1")){
                        condition.setAuthor(author);
                        condition.setSortBy(Book.SortColumn.AUTHOR);
                    }
                    System.out.println("输入最小图书价格(如果不用该类型的查询，请输入-1):");
                    String temp5 = scanner.nextLine();
                    Double minprice = Double.parseDouble(temp5);
                    if(minprice!=-1){
                        condition.setMinPrice(minprice);
                        condition.setSortBy(Book.SortColumn.PRICE);
                    }
                    System.out.println("输入最大图书价格(如果不用该类型的查询，请输入-1):");
                    String temp6 = scanner.nextLine();
                    Double maxprice = Double.parseDouble(temp6);
                    if(maxprice!=-1){
                        condition.setMaxPrice(maxprice);
                        condition.setSortBy(Book.SortColumn.PRICE);
                    }
                    System.out.println("输入升序（1）或降序（0），请输入1/0:");
                    String temp4 = scanner.nextLine();
                    int input = Integer.parseInt(temp4);
                    if(input==1){
                        condition.setSortOrder(SortOrder.ASC);
                    }
                    else if(input==0){
                        condition.setSortOrder(SortOrder.DESC);
                    }
                    ApiResult result = library.queryBook(condition);
                    if(result.ok){
                        BookQueryResults results=(BookQueryResults)library.queryBook(condition).payload;
                        System.out.println("编号\t类别\t书名\t出版社\t出版年份\t价格\t库存");
                        for(int i=0;i<results.getResults().size();i++){
                            Book b=results.getResults().get(i);
                            System.out.println(b.getBookId()+"\t"+b.getCategory()+"\t"+b.getTitle()+"\t"+b.getPress()+"\t"+b.getPublishYear()+"\t"+b.getPrice()+"\t6"+b.getStock());
                        }
                        System.out.println("查询成功");
                    }
                    else{
                        System.out.println("查询成功");
                    }
                }
                else if(a==7){
                    System.out.println("7.借书模块");
                    System.out.println("请输入图书编号：");
                    String temp1 = scanner.nextLine();
                    int book_id = Integer.parseInt(temp1);
                    System.out.println("请输入借书证编号：");
                    String temp2 = scanner.nextLine();
                    int card_id = Integer.parseInt(temp2);
                    Borrow borrow=new Borrow(book_id,card_id);
                    borrow.resetBorrowTime();
                    ApiResult result = library.borrowBook(borrow);
                    if(result.ok){
                        System.out.println("借书成功");
                    }
                    else{
                        System.out.println("借书失败，该图书尚未归还");
                    }
                }
                else if(a==8){
                    System.out.println("8.借书记录查询");
                    System.out.println("请输入卡片编号：");
                    String temp1 = scanner.nextLine();
                    int card_id = Integer.parseInt(temp1);
                    ApiResult result = library.showBorrowHistory(card_id);
                    if(result.ok){
                        BorrowHistories results=(BorrowHistories) library.showBorrowHistory(card_id).payload;
                        System.out.println("图书编号\t借书证编号\t借书时间\t还书时间");
                        for(int i=0;i<results.getItems().size();i++){
                            System.out.println(results.getItems().get(i).getBookId()+"\t"+results.getItems().get(i).getCardId()+"\t"+results.getItems().get(i).getBorrowTime()+"\t"+results.getItems().get(i).getReturnTime());
                        }
                        System.out.println("查询成功");
                    }
                    else{
                        System.out.println("查询失败");
                    }
                }
                else if(a==9) {
                    Card card = new Card();
                    System.out.println("9.借书证注册");
                    System.out.println("请输入姓名：");
                    String name = scanner.nextLine();
                    card.setName(name);
                    System.out.println("请输入系：");
                    String department = scanner.nextLine();
                    card.setDepartment(department);
                    System.out.println("请输入卡片类型（S表示学生，T表示老师)：");
                    String type = scanner.nextLine();
                    if (type .equals("S") ) {
                        card.setType(Card.CardType.values("S"));
                    }
                    if (type .equals("T") ) {
                        card.setType(Card.CardType.values("T"));
                    }
                    ApiResult result = library.registerCard(card);
                    if (result.ok) {
                        System.out.println("注册成功");
                    } else {
                        System.out.println("注册失败");
                    }
                }

                else if(a==10){
                        System.out.println("10.删除借书证");
                        System.out.println("请输入卡片编号：");
                        String temp1 = scanner.nextLine();
                        int card_id = Integer.parseInt(temp1);
                        ApiResult result = library.removeCard(card_id);
                        if(result.ok){
                            System.out.println("删除成功");
                        }
                        else{
                            System.out.println("删除失败");
                        }
                }
                else if(a==11){
                    System.out.println("11.借书证查询");
                    ApiResult result = library.showCards();
                    if(result.ok){
                        CardList resultss=(CardList)library.showCards().payload;
                        System.out.println("编号\t姓名\t系\t类型");
                        for(int i=0;i< resultss.getCards().size();i++){
                            Card b=resultss.getCards().get(i);
                            System.out.println(b.getCardId()+"\t"+b.getName()+"\t"+b.getDepartment()+"\t"+b.getType());
                        }
                        System.out.println("查询成功");
                    }
                    else{
                        System.out.println("查询失败");
                    }
                }
                else if(a==13){
                    System.out.println("欢迎下次使用！");
                    break;
                }
                else if(a==14){
                    ApiResult result=library.resetDatabase();
                }
                else if(a==12){
                    System.out.println("12.还书模块");
                    System.out.println("请输入图书编号：");
                    String temp1 = scanner.nextLine();
                    int book_id = Integer.parseInt(temp1);
                    System.out.println("请输入借书证编号：");
                    int card_id = scanner.nextInt();
                    Borrow borrow=new Borrow(book_id,card_id);
                    ApiResult result1=library.showBorrowHistory(card_id);
                    if(result1.ok){
                        borrow.resetReturnTime();
                        BorrowHistories results=(BorrowHistories) library.showBorrowHistory(card_id).payload;
                        for(int i=0;i<results.getItems().size();i++){
                            if(card_id==results.getItems().get(i).getCardId()&&book_id==results.getItems().get(i).getBookId()){
                                borrow.setBorrowTime(results.getItems().get(i).getBorrowTime());
                                break;
                            }
                        }
                        ApiResult result2 = library.returnBook(borrow);
                        if(result2.ok){
                            System.out.println("还书成功");
                        }
                        else{
                            System.out.println("还书失败");
                        }
                    }
                    else{
                        System.out.println("还书失败");
                    }
                }
            }
            // release database connection handler
            if (connector.release()) {
                log.info("Success to release connection.");
            } else {
                log.warning("Failed to release connection.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
