**Custom Paging With Filter** is a library that makes it easy to implement paginated lists and add the ability to filter these lists.

To start working with the library, you need to create 2 classes.

-----------------------------

1. Class that inherits from **SearchableDataSourceFactory** *(named UserSourceFactory in the sample).*

You should implement such methods as:

- **getDataSource** - method helps us to get the ability to build any queues with parameters and apply them to the Room database, which will return the filtered DataSource.Factory.

- **onDataLoaded, onItemsInsterted, onitemsDeleted**  - in these methods we can perform any actions with the data received from the server. For instance. write them to the database, for convenience this class already contains dao for writing.

------------------------

2. Class inherited from **BaseRefreshableRepository** *(named UserListRepository in the sample).*

This class contains the main interaction with the framework. First, you need to implement 4 methods:

- **validateQueryKey** - in this method we describe all the names of the parameters which can be apply to the future list and compare them with the current queryKey, return true if queryKey matches with any parameter name, and false if this name does not found in the method.

- **loadData** - in this method we build a searchQuery for the API and make a filtered request to the server. The loadData method expects a return value as response from the server to further transform this data.

- **insertItemsApi, deleteItemsApi** - in these methods we perform insert or delete items from the server. The return value expects ResponseModel, which could give us information about response result (success or failure)

-------------------------

**BaseRefreshableRepository** also has methods for managing parameters.

To add new parameters to the list filter, we need to call **setQuery** to insert one parameter and its values, or **setQueries** to immediately add a list of parameters and their values.

To get information about the current parameters applied to the list, you must use the **getQuery** and **getQueries** methods, which return the parameters and their values

To clear all current parameters, use the **clearQueries** method.

In order to observe data changes and update them on the screen, you must use the **getItems** method, which returns LiveData <PagedList <T>>

It is also possible to refresh data from the server using the **refresh** method

If we need to synchronize work or perform any actions in the process of loading data, then for these purposes there is a listener that can be initialized with the **setOnLoadListener** method

By default, the framework already contains the default pagination settings, but they can be customized. To do this, override the **pagedConfig** property, but if you only need to change the basic parameters such as **PAGE, PAGE_SIZE, DISTANCE, INITIAL_PAGE**, then your values ​​can be passed to the base class constructor during inheritance.

All methods, their description, implementation and additional information about the input parameters can be found by looking at the sample and the source code of the framework which contains description before each method.
