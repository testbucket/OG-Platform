<#escape x as x?html>
{
    "header" : {
        "type":"Configs",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "dataFields":["id","name"<#if type = ''>,"type"</#if>]
    },
    "data" : [<#if searchResult??>
       <#list searchResult.documents as item>
           "${item.uniqueId.objectId}|${item.name}<#if type = ''>|${item.value.class.simpleName}</#if>"<#if item_has_next>,</#if>
       </#list>
    </#if>]
}
</#escape>