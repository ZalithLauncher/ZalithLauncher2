#ifndef UNORDERED_MAP_H
#define	UNORDERED_MAP_H

#include <stdlib.h>
#include <stdbool.h>

#ifdef	__cplusplus
extern "C" {
#endif





typedef struct unordered_map_entry {
    void*                       key;
    void*                       value;
    struct unordered_map_entry* chain_next;
    struct unordered_map_entry* prev;
    struct unordered_map_entry* next;
} unordered_map_entry;

struct unordered_map {
    unordered_map_entry** table;
    unordered_map_entry*  head;
    unordered_map_entry*  tail;
    size_t              (*hash_function)(void*);
    bool                (*equals_function)(void*, void*);
    size_t                mod_count;
    size_t                table_capacity;
    size_t                size;
    size_t                max_allowed_size;
    size_t                mask;
    float                 load_factor;
};

typedef struct unordered_map          unordered_map;

struct unordered_map_iterator {
    unordered_map*       map;
    unordered_map_entry* next_entry;
    size_t               iterated_count;
    size_t               expected_mod_count;
};

typedef struct unordered_map_iterator unordered_map_iterator;

/***************************************************************************
* Allocates a new, empty map with given hash function and given equality   *
* testing function.                                                        *
***************************************************************************/
unordered_map* unordered_map_alloc
        (size_t   initial_capacity,
         float    load_factor,
         size_t (*hash_function)(void*),
         bool   (*equals_function)(void*, void*));

/***************************************************************************
* If p_map does not contain the key p_key, inserts it in the map,          *
* associates p_value with it and return NULL. Otherwise updates the value  *
* and returns the old value.                                               *
***************************************************************************/
void* unordered_map_put (unordered_map* map, void* key, void* value);

/***************************************************************************
* Returns a positive value if p_key is mapped to some value in this map.   *
***************************************************************************/
bool unordered_map_contains_key (unordered_map* map, void* key);

/***************************************************************************
* Returns the value associated with the p_key, or NULL if p_key is not     *
* mapped in the map.                                                       *
***************************************************************************/
void* unordered_map_get (unordered_map* map, void* key);

/***************************************************************************
* If p_key is mapped in the map, removes the mapping and returns the value *
* of that mapping. If the map did not contain the mapping, returns NULL.   *
***************************************************************************/
void* unordered_map_remove (unordered_map* map, void* p_key);

/***************************************************************************
* Removes all the contents of the map.                                     *
***************************************************************************/
void unordered_map_clear (unordered_map* map);

/***************************************************************************
* Returns the size of the map, or namely, the amount of key/value mappings *
* in the map.                                                              *
***************************************************************************/
size_t unordered_map_size (unordered_map* map);

/***************************************************************************
* Checks that the map is in valid state.                                   *
***************************************************************************/
bool unordered_map_is_healthy (unordered_map* map);

/***************************************************************************
* Deallocates the entire map. Only the map and its nodes are deallocated.  *
* The user is responsible for deallocating the actual data stored in the   *
* map.                                                                     *
***************************************************************************/
void unordered_map_free (unordered_map* map);

/***************************************************************************
* Returns the iterator over the map. The entries are iterated in insertion *
* order.                                                                   *
***************************************************************************/
unordered_map_iterator* unordered_map_iterator_alloc
        (unordered_map* map);
bool unordered_map_iterator_alloc_local(unordered_map* map, unordered_map_iterator* iterator);

/***************************************************************************
* Returns the number of keys not yet iterated over.                        *
***************************************************************************/
size_t unordered_map_iterator_has_next
        (unordered_map_iterator* iterator);

/***************************************************************************
* Loads the next entry in the iteration order.                             *
***************************************************************************/
bool unordered_map_iterator_next(unordered_map_iterator* iterator,
                                 void** key_pointer,
                                 void** value_pointer);

/***************************************************************************
* Returns a true if the map was modified during the iteration.             *
***************************************************************************/
bool unordered_map_iterator_is_disturbed
        (unordered_map_iterator* iterator);

/***************************************************************************
* Deallocates the map iterator.                                            *
***************************************************************************/
void unordered_map_iterator_free(unordered_map_iterator* iterator);

#ifdef	__cplusplus
}
#endif

#endif	/* UNORDERED_MAP_H */
