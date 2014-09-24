"""ORM Wrappers"""
import inspect
from webhelpers.util import Partial

orms = {}
try:
    import sqlobject
except:
    pass
else:
    orms['sqlobject'] = True
try:
    import sqlalchemy
except:
    pass
else:
    orms['sqlalchemy'] = True

def get_wrapper(obj, *args, **kw):
    if isinstance(obj, (list, tuple)):
        return obj
    if orms.get('sqlobject'):
        if inspect.isclass(obj) and issubclass(obj, sqlobject.SQLObject):
            return SQLObjectLazy(obj.select, *args, **kw)
    if orms.get('sqlalchemy'):
        if hasattr(obj, '_is_primary_mapper'):
            return SQLAlchemyLazyMapper(obj, *args, **kw)
        if isinstance(obj, sqlalchemy.Table):
            return SQLAlchemyLazyTable(obj, *args, **kw)
    return "You shouldn't have this"
    

class SQLObjectLazy(Partial):
    def __getitem__(self, key):
        if not isinstance(key, slice):
            raise Exception, "SQLObjectLazy doesn't support getitem without slicing"
        return list(self()[key.start:key.stop])
    
    def __len__(self):
        return self().count()

class SQLAlchemyLazyTable(Partial):
    def __getitem__(self, key):
        if not isinstance(key, slice):
            raise Exception, "SQLAlchemyLazy doesn't support getitem without slicing"
        limit = key.stop - key.start
        offset = key.start
        fn = self.fn
        self.fn = fn.select
        results = self(limit=limit, offset=offset).execute()
        self.fn = fn
        return results
    
    def __len__(self):
        s = self.fn.select(*self.args, **self.kw)
        return self.fn([func.count(1)], from_obj=[s])

class SQLAlchemyLazyMapper(Partial):
    def __getitem__(self, key):
        if not isinstance(key, slice):
            raise Exception, "SQLAlchemyLazy doesn't support getitem without slicing"
        limit = key.stop - key.start
        offset = key.start
        fn = self.fn
        self.fn = fn.select
        results = self(limit=limit, offset=offset)
        self.fn = fn
        return results
    
    def __len__(self):
        return self.fn.count(*self.args, **self.kw)
