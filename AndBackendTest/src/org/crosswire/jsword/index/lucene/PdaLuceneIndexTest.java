package org.crosswire.jsword.index.lucene;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import junit.framework.TestCase;
import net.bible.service.sword.SwordDocumentFacade;

import org.crosswire.common.util.CWProject;
import org.crosswire.common.util.NetUtil;
import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.book.BookData;
import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.book.BookMetaData;
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.book.FeatureType;
import org.crosswire.jsword.book.OSISUtil;
import org.crosswire.jsword.book.sword.SwordBookDriver;
import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.crosswire.jsword.passage.PassageKeyFactory;
import org.jdom.Element;

public class PdaLuceneIndexTest extends TestCase {

	private Book[] books;
	private SwordDocumentFacade swordApi;

	protected void setUp() throws Exception {
		super.setUp();
		SwordDocumentFacade.setAndroid(false);
		swordApi = SwordDocumentFacade.getInstance();
		
        SwordBookDriver swordBookDriver = new SwordBookDriver();
        books = swordBookDriver.getBooks();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIndexGetPath() {
		try {
			Book book = getBook("ESV");
			URI indexPath = getIndexStorageArea(book);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testFind() {
		try {
			Book book = getBook("ESV");

			Locale.setDefault(new Locale("nn"));
			
			doFindTest(book, "rebekah", 29);

			doFindTest(book, "bless", 120);

			doFindTest(book, "+[Mat-Rev] blessing", 22);

			doFindTest(book, "+[Mat-Rev] +Noah", 8);

			doFindTest(book, "key:John.1.1", 1);

			doFindTest(book, "month", 183);

			doFindTest(book, "+[Mat-Rev] +John", 132);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void testGetStrongs() throws NoSuchKeyException, BookException {
		Book book = Books.installed().getBook("KJV");
		assertTrue("Should have Strongs", book.getBookMetaData().hasFeature(FeatureType.STRONGS_NUMBERS));
		
		Key key = book.getKey("Gen 1:1");
		BookData data = new BookData(book, key);
		Element osis = data.getOsisFragment();
		
		String strongsNumbers = OSISUtil.getStrongsNumbers(osis);
		assertTrue("No Strongs in KJV", strongsNumbers.length()>0);
	}

	private void doFindTest(Book book, String test, int expected) throws BookException {
		Key key = book.find(test);
		System.out.println(test+" found "+key.getCardinality()+" occurences: "+key.getName());
		assertEquals(test+" find count wrong", expected, key.getCardinality());
		
	}
    private static final String DIR_LUCENE = "lucene"; //$NON-NLS-1$
	private URI getIndexStorageArea(Book book) throws IOException {
        BookMetaData bmd = book.getBookMetaData();
        String driverName = bmd.getDriverName();
        String bookName = bmd.getInitials();

        assert driverName != null;
        assert bookName != null;

        URI base = CWProject.instance().getWriteableProjectSubdir(DIR_LUCENE, false);
        URI driver = NetUtil.lengthenURI(base, driverName);

        return NetUtil.lengthenURI(driver, bookName);
    }

	private Book getBook(String initials) {
		for (Book book : books) {
			if (book.getInitials().equals(initials)) {
				System.out.print("Found:"+book.getName());
				return book;
			}
		}
		return null;
	}
}
