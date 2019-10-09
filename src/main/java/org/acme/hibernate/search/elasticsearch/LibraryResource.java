package org.acme.hibernate.search.elasticsearch;

import io.quarkus.runtime.StartupEvent;
import org.hibernate.search.mapper.orm.Search;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LibraryResource {

    @Inject
    EntityManager em;

    @Transactional
    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        // only reindex if we imported some content
        if (Book.count() > 0) {
            Search.session(em)
                    .massIndexer()
                    .startAndWait();
        }
    }

    @GET
    @Path("author/search")
    @Transactional
    public List<Author> searchAuthors(@QueryParam("pattern") String pattern) {
        return Search.session(em)
                .search(Author.class)
                .predicate(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .onFields("firstName", "lastName", "books.title").matching(pattern)
                )
                .sort(f -> f.byField("lastName_sort").then().byField("firstName_sort"))
                .fetchHits();
    }


    @PUT
    @Path("book")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addBook(@FormParam("title") String title, @FormParam("authorId") Long authorId) {
        Author author = Author.findById(authorId);
        if (author == null) {
            return;
        }

        Book book = new Book();
        book.title = title;
        book.author = author;
        book.persist();
    }
    @DELETE
    @Path("book/{id}")
    @Transactional
    public void deleteBook(@PathParam("id") Long id) {
        Book book = Book.findById(id);
        System.out.println("hi");
        if (book != null) {
            book.author.books.remove(book);
            book.delete();
        }
    }

    @PUT
    @Path("author")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addAuthor(@FormParam("firstName") String firstName, @FormParam("lastName") String lastName) {
        Author author = new Author();
        author.firstName = firstName;
        author.lastName = lastName;
        author.persist();
    }

    @POST
    @Path("author/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void updateAuthor(@PathParam("id") Long id, @FormParam("firstName") String firstName, @FormParam("lastName") String lastName) {
        Author author = Author.findById(id);
        if (author == null) {
            return;
        }
        author.firstName = firstName;
        author.lastName = lastName;
        author.persist();
    }

    @DELETE
    @Path("author/{id}")
    @Transactional
    public void deleteAuthor(@PathParam("id") Long id) {
        Author author = Author.findById(id);
        if (author != null) {
            author.delete();
        }
    }
}