package org.acme.hibernate.search.elasticsearch;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Indexed
public class Author extends PanacheEntity {
    @FullTextField(analyzer = "name")
    @KeywordField(name = "firstName_sort", sortable = Sortable.YES, normalizer = "sort")
    public String firstName;

    @FullTextField(analyzer = "name")
    @KeywordField(name = "lastName_sort", sortable = Sortable.YES, normalizer = "sort")
    public String lastName;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @IndexedEmbedded
    public Set<Book> books;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Author)) {
            return false;
        }

        Author other = (Author) o;

        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

}
