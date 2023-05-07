
module.exports = function(__runtime__, scope){
    var storages = {};
    storages.create = function(){
        return new LocalStorage();
    }
    storages.remove = function(name){
        this.create(name).clear();
    }
    return storages;
    function LocalStorage(){
        this._storage = new com.stardust.autojs.core.storage.LocalStorage(context);
        this.get = function(key, defaultValue){
            var value = this._storage.getString(key, null);
            if(!value){
                return defaultValue;
            }
            return JSON.parse(value);
        }
        this.contains = function(key){
            return this._storage.contains(key);
        }
    }
}

