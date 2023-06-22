import entities.Book;
import entities.Borrow;
import entities.Card;
import queries.*;
import utils.DBInitializer;
import utils.DatabaseConnector;
import java.util.ArrayList;
import java.sql.*;
import java.util.List;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {
        Connection conn = connector.getConn();
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        try {
            String sql1 = "SELECT * FROM book WHERE category=? AND title=? AND press=? AND publish_year=? AND author=?";
            stmt1 = conn.prepareStatement(sql1);
            stmt1.setString(1, book.getCategory());
            stmt1.setString(2, book.getTitle());
            stmt1.setString(3, book.getPress());
            stmt1.setInt(4, book.getPublishYear());
            stmt1.setString(5, book.getAuthor());
            rs = stmt1.executeQuery();
            if (rs.next()) {
                return new ApiResult(false, "Unimplemented Function");
            }
            else{
                String sql2 = "INSERT INTO book (category, title, press, publish_year, author, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
                stmt2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS);
                stmt2.setString(1, book.getCategory());
                stmt2.setString(2, book.getTitle());
                stmt2.setString(3, book.getPress());
                stmt2.setInt(4, book.getPublishYear());
                stmt2.setString(5, book.getAuthor());
                stmt2.setDouble(6, book.getPrice());
                stmt2.setInt(7, book.getStock());
                int len = stmt2.executeUpdate();
                // If the insertion failed, return an error message.
                assert (len==1);
                commit(conn);
                // Retrieve the generated book ID and update the book object.
                ResultSet rs2 = stmt2.getGeneratedKeys();
                if (rs2.next()) {
                    book.setBookId(rs2.getInt(1));
                }
                return new ApiResult(true, null);
            }
        }
        catch (SQLException e) {
            rollback(conn);
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // Establish a connection to the database.
            conn = connector.getConn();
            // Get the current stock of the book from the database.
            String sql1 = "SELECT stock FROM book WHERE book_id=?";
            stmt = conn.prepareStatement(sql1);
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()==false) {
                return new ApiResult(false, "Unimplemented Function");
            }
            int oldStock = rs.getInt("stock");
            int newStock = oldStock + deltaStock;
            if (newStock < 0) {
                newStock = 0;
                return new ApiResult(false, "Unimplemented Function");
            }
            String sql2 = "UPDATE book SET stock=? WHERE book_id=?";
            stmt = conn.prepareStatement(sql2);
            stmt.setInt(1, newStock);
            stmt.setInt(2, bookId);
            int len = stmt.executeUpdate();
            assert (len==1);
            commit(conn);
            return new ApiResult(true, null);
        } catch (SQLException e) {
            rollback(conn);
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        String sqlSelect = "SELECT * FROM book WHERE category=? AND title=? AND press=? AND publish_year=? AND author=?";
        String sqlInsert = "INSERT INTO book (category, title, press, publish_year, author, price, stock) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement stmt2 =null;
        try {
            conn = connector.getConn();
            pstmt = conn.prepareStatement(sqlSelect);
            int temp=1;
            stmt2 = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);
            for (Book book : books) {
                // 检查是否之前已有记录
                pstmt.setString(1, book.getCategory());
                pstmt.setString(2, book.getTitle());
                pstmt.setString(3, book.getPress());
                pstmt.setInt(4, book.getPublishYear());
                pstmt.setString(5, book.getAuthor());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() ) {
                    conn.rollback();
                    return new ApiResult(false, "Unimplemented Function");
                }
                // 添加到批处理
                stmt2.setString(1, book.getCategory());
                stmt2.setString(2, book.getTitle());
                stmt2.setString(3, book.getPress());
                stmt2.setInt(4, book.getPublishYear());
                stmt2.setString(5, book.getAuthor());
                stmt2.setDouble(6, book.getPrice());
                stmt2.setInt(7, book.getStock());
                ResultSet rs2 = stmt2.getGeneratedKeys();
                if (rs2.next()) {
                    book.setBookId(rs2.getInt(1));
                    //System.out.println(book.getCategory());
                }
                stmt2.addBatch();
            }
            int[] results = stmt2.executeBatch();
            for (int result : results) {
                if (result < 1) {
                    return new ApiResult(false, "Unimplemented Function");
                }
            }
            ResultSet rs2 = stmt2.getGeneratedKeys() ;
                for (Book book : books) {
                    if (rs2.next()) {
                        int bookId = rs2.getInt(1);
                        book.setBookId(bookId);
                    }
                }

            conn.commit();
            return new ApiResult(true, null);
        }
        catch (SQLException e) {
            rollback(conn);
            //System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            conn = connector.getConn();
            String sql1 = "SELECT COUNT(*) AS cnt FROM borrow WHERE book_id=? AND return_time =0";
            stmt1 = conn.prepareStatement(sql1);
            stmt1.setInt(1, bookId);
            ResultSet rs1 = stmt1.executeQuery();
            int count = 0;
            if (rs1.next()) {
                count = rs1.getInt("cnt");
            }
            if (count > 0) {
                return new ApiResult(false, "Unimplemented Function");
            }
            String sql2 = "DELETE FROM book WHERE book_id=?";
            stmt2 = conn.prepareStatement(sql2);
            stmt2.setInt(1, bookId);
            int rs = stmt2.executeUpdate();
            if (rs == 0) {
                return new ApiResult(false, "Unimplemented Function");
            }
            // Book is deleted successfully
            commit(conn);
            return new ApiResult(true, null);
        } catch (SQLException e) {
            rollback(conn);
            System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connector.getConn();
            String sql1 = "SELECT COUNT(*) AS cnt FROM book WHERE book_id=? AND stock=?";
            stmt = conn.prepareStatement(sql1);
            stmt.setInt(1, book.getBookId());
            stmt.setInt(2, book.getStock());
            ResultSet rs1 = stmt.executeQuery();
            if (!rs1.next()) {
                return new ApiResult(false, "Unimplemented Function");
            }
            String sql2 = "UPDATE book SET category=?, title=?, press=?, publish_year=?, author=?, price=? WHERE book_id=?";
            stmt = conn.prepareStatement(sql2);
            stmt.setString(1, book.getCategory());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getPress());
            stmt.setInt(4, book.getPublishYear());
            stmt.setString(5, book.getAuthor());
            stmt.setDouble(6, book.getPrice());
            stmt.setInt(7, book.getBookId());
            int len = stmt.executeUpdate();
            assert (len==1);
            commit(conn);
            return new ApiResult(true,null);
        } catch (SQLException e) {
            rollback(conn);
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Establish a connection to the database.
            conn = connector.getConn();
            // Construct the SQL SELECT statement based on the given conditions.
            String sql = "SELECT * FROM book WHERE 1=1";
            if (conditions.getCategory() != null) {
                sql += " AND category=?";
            }
            if (conditions.getTitle() != null) {
                sql += " AND title LIKE ?";
            }
            if (conditions.getPress() != null) {
                sql += " AND press LIKE ?";
            }
            if (conditions.getMinPublishYear() != null) {
                sql += " AND publish_year>=?";
            }
            if (conditions.getMaxPublishYear() != null) {
                sql += " AND publish_year<=?";
            }
            if (conditions.getAuthor() != null) {
                sql += " AND author LIKE ?";
            }
            if (conditions.getMinPrice() != null) {
                sql += " AND price>=?";
            }
            if (conditions.getMaxPrice() != null) {
                sql += " AND price<=?";
            }
            sql += " ORDER BY "+conditions.getSortBy().getValue() +" "+conditions.getSortOrder().getValue()+", book_id ASC";
            // Create a prepared statement with placeholder parameters.
            stmt = conn.prepareStatement(sql);
            // Bind the query conditions to the prepared statement.
            int index = 1;
            if (conditions.getCategory() != null) {
                stmt.setString(index++, conditions.getCategory());
            }
            if (conditions.getTitle() != null) {
                stmt.setString(index++, "%" + conditions.getTitle() + "%");
            }
            if (conditions.getPress() != null) {
                stmt.setString(index++, "%" +conditions.getPress()+ "%");
            }
            if (conditions.getMinPublishYear() != null) {
                stmt.setInt(index++, conditions.getMinPublishYear());
            }
            if (conditions.getMaxPublishYear() != null) {
                stmt.setInt(index++, conditions.getMaxPublishYear());
            }
            if (conditions.getAuthor() != null) {
                stmt.setString(index++, "%" + conditions.getAuthor() + "%");
            }
            if (conditions.getMinPrice() != null) {
                stmt.setDouble(index++, conditions.getMinPrice());
            }
            if (conditions.getMaxPrice() != null) {
                stmt.setDouble(index++, conditions.getMaxPrice());
            }

            // Execute the SQL SELECT statement and return the results.
            ResultSet rs = stmt.executeQuery();
            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                Book book = new Book(
                        rs.getString("category"),
                        rs.getString("title"),
                        rs.getString("press"),
                        rs.getInt("publish_year"),
                        rs.getString("author"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                );
                book.setBookId(rs.getInt("book_id"));
                books.add(book);
            }
            BookQueryResults results = new BookQueryResults(books);
            commit(conn);
            return new ApiResult(true, results);
        } catch (SQLException e) {
            rollback(conn);
            //System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        PreparedStatement stmt5 = null;
        ResultSet rs = null;
        try {
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn = connector.getConn();
            // Check if the book exists and its stock is greater than 0
            String sql1 = "SELECT * FROM book WHERE book_id=?";
            stmt1 = conn.prepareStatement(sql1);
            stmt1.setInt(1, borrow.getBookId());
            rs = stmt1.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Unimplemented Function");
            }
            int stock = rs.getInt("stock");
            if (stock == 0) {
                // Book out of stock
                return new ApiResult(false, "Unimplemented Function");
            }
            // Check if the card exists
            String sql2 = "SELECT * FROM card WHERE card_id=? ";
            stmt2 = conn.prepareStatement(sql2);
            stmt2.setInt(1, borrow.getCardId());
            rs = stmt2.executeQuery();
            if (!rs.next()) {
                // Card not exists
                return new ApiResult(false, "Unimplemented Function");
            }
            // Check if the book has been borrowed but not returned by this user
            String sql3 = "SELECT * FROM borrow WHERE book_id=? AND card_id=? AND return_time=0 ";
            stmt3 = conn.prepareStatement(sql3);
            stmt3.setInt(1, borrow.getBookId());
            stmt3.setInt(2, borrow.getCardId());
            rs = stmt3.executeQuery();
            if (rs.next()) {
                // This book has been borrowed but not returned by this user
                return new ApiResult(false, "Unimplemented Function");
            }
            // Insert a new borrow record and update the stock of the book
            String sql4 = "INSERT INTO borrow(book_id, card_id, borrow_time, return_time) VALUES (?, ?, ?,?)";
            stmt4 = conn.prepareStatement(sql4);
            stmt4.setInt(1, borrow.getBookId());
            stmt4.setInt(2,borrow.getCardId());
            stmt4.setLong(3, borrow.getBorrowTime());
            stmt4.setInt(4, 0);
            int len = stmt4.executeUpdate();
            assert (len==1);
            String sql5 = "UPDATE book SET stock=stock-1 WHERE book_id=?";
            stmt5 = conn.prepareStatement(sql5);
            stmt5.setInt(1, borrow.getBookId());
            len = stmt5.executeUpdate();
            assert (len==1);
            commit(conn);
            return new ApiResult(true, null);
        }
        catch (SQLException e) {
            rollback(conn);
            //System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
          }
    }
    private ApiResult close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                return new ApiResult(false, "Unimplemented Function");
            }
        }
        return new ApiResult(true, null);
    }

    private ApiResult close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                return new ApiResult(false, "Unimplemented Function");
            }
        }
        return new ApiResult(true, null);
    }
    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connector.getConn();
            // Find the borrow record and update the return time
            String sql = "SELECT * FROM borrow WHERE book_id=? AND card_id=? AND borrow_time=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, borrow.getBookId());
            stmt.setInt(2,borrow.getCardId());
            stmt.setLong(3, borrow.getBorrowTime());
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                // No matching borrow record found
                return new ApiResult(false, "Unimplemented Function");
            }
            Long borrowTime = rs.getLong("borrow_time");
            Long returnTime = borrow.getReturnTime();
            if (returnTime<borrowTime) {
                // Invalid return time
                return new ApiResult(false, "Unimplemented Function");
            }
            String sql2 = "UPDATE borrow SET return_time=? WHERE book_id=? AND card_id=? AND borrow_time=?";
            stmt = conn.prepareStatement(sql2);
            stmt.setLong(1, returnTime);
            stmt.setInt(2, borrow.getBookId());
            stmt.setInt(3, borrow.getCardId());
            stmt.setLong(4, borrow.getBorrowTime());
            int len = stmt.executeUpdate();
            assert (len==1);
            // Update the stock of the book
            sql = "UPDATE book SET stock=stock+1 WHERE book_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, borrow.getBookId());
            len = stmt.executeUpdate();
            assert (len==1);
            commit(conn);
            return new ApiResult(true, null);
        } catch (SQLException e) {
            rollback(conn);
            //System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");}
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connector.getConn();
            // Find all borrow records of this card, sorted by borrow_time DESC, book_id ASC
            String sql = "SELECT * FROM borrow WHERE card_id=? ORDER BY borrow_time DESC, book_id ASC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();
            // Create a BorrowHistories object to hold the query results
            List<BorrowHistories.Item> items = new ArrayList<BorrowHistories.Item>();
            while (rs.next()) {
                String sql1 = "SELECT * FROM book WHERE book_id=?";
                stmt = conn.prepareStatement(sql1);
                stmt.setInt(1, rs.getInt("book_id"));
                ResultSet rs_book = stmt.executeQuery();
                if(!rs_book.next()){
                    return new ApiResult(false, "Unimplemented Function");
                }
                Book book = new Book(
                        rs_book.getString("category"),
                        rs_book.getString("title"),
                        rs_book.getString("press"),
                        rs_book.getInt("publish_year"),
                        rs_book.getString("author"),
                        rs_book.getDouble("price"),
                        rs_book.getInt("stock")
                );
                book.setBookId(rs_book.getInt("book_id"));
                String sql2 = "SELECT * FROM borrow WHERE book_id=? AND card_id= ? AND borrow_time=?";
                stmt = conn.prepareStatement(sql2);
                stmt.setInt(1, rs.getInt("book_id"));
                stmt.setInt(2, cardId);
                stmt.setLong(3, rs.getLong("borrow_time"));
                ResultSet rs_borrow = stmt.executeQuery();
                if(!rs_borrow.next()){
                    return new ApiResult(false, "Unimplemented Function");
                }
                Borrow borrow = new Borrow(
                );
                borrow.setBookId(rs_borrow.getInt("book_id"));
                borrow.setCardId(cardId);
                borrow.setBorrowTime(rs_borrow.getLong("borrow_time"));
                borrow.setReturnTime(rs_borrow.getLong("return_time"));
                BorrowHistories.Item temp=new BorrowHistories.Item(cardId,book,borrow);
                items.add(temp);
            }

            BorrowHistories result= new BorrowHistories(items);
            commit(conn);
            return new ApiResult(true, result);
        } catch (SQLException e) {
            rollback(conn);
            //System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connector.getConn();
            String sql = "SELECT * FROM card WHERE name=? AND department=? AND type=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Card already exists
                return new ApiResult(false, "Unimplemented Function");
            }
            // Insert a new card record
            String sql2 = "INSERT INTO card(name, department, type) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql2,Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, card.getName());
            stmt.setString(2, card.getDepartment());
            stmt.setString(3, card.getType().getStr());
            int len = stmt.executeUpdate();
            assert (len==1);
            ResultSet rs2=stmt.getGeneratedKeys();
            if (rs2.next()) {
                card.setCardId(rs2.getInt(1));
            }
            commit(conn);
            return new ApiResult(true, null);
        } catch (SQLException e) {
            rollback(conn);
            System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connector.getConn();
            String sql = "SELECT * FROM card WHERE card_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return new ApiResult(false, "Unimplemented Function");
            }
            String sql3 = "SELECT * FROM borrow WHERE card_id= ? AND return_time= ? ";
            stmt = conn.prepareStatement(sql3);
            stmt.setInt(1, cardId);
            stmt.setInt(2, 0);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new ApiResult(false, "Unimplemented Function");
            }
            // selete a card record
            String sql2 = "DELETE FROM card WHERE card_id=?";
            stmt = conn.prepareStatement(sql2);
            stmt.setInt(1, cardId);
            int len = stmt.executeUpdate();
            assert (len==1);
            commit(conn);
            return new ApiResult(true, null);
        } catch (SQLException e) {
            rollback(conn);
            //System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult showCards() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connector.getConn();
            String sql = "SELECT * FROM card ORDER BY card_id";
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            List<Card> cards = new ArrayList<>();
            while (rs.next()) {
                int cardId = rs.getInt("card_id");
                String name = rs.getString("name");
                String department = rs.getString("department");
                Card card = new Card(cardId, name, department, Card.CardType.values(rs.getString("type")));
                cards.add(card);
            }
            CardList cardList = new CardList(cards);
            commit(conn);
            return new ApiResult(true, cardList);
        } catch (SQLException e) {
            rollback(conn);
            //System.out.println(e.getMessage());
            return new ApiResult(false, "Unimplemented Function");
        }
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
