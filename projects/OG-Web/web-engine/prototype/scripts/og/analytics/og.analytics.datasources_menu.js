
$.register_module({
    name: 'og.analytics.DatasourcesMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () { 
        return function (config) {
            var menu = new og.analytics.DropMenu(config), $dom = menu.$dom, opts = menu.opts, data = menu.data,
                ds_opts = [], $ds_selection = $('.datasources-selection', $dom.title), ds_val, type_val, sel_pos,
                $sel_parent, $type_select, $ds_select, $sel_checkbox, default_sel_txt = 'select data source...',
                del_s = '.og-icon-delete', options_s = '.OG-dropmenu-options', type_s = '.type', ds_s = '.source',
                select_s = 'select', checkbox_s = '.og-option :checkbox', $sel_opt = $('<div>').append('<option>'),
                menu_click_s = 'input, div.og-icon-delete, a.OG-link-add', menu_change_s = select_s,
                populate_ds_opts = function () {
                    $ds_select.empty().append($($sel_opt.html()).text(default_sel_txt));
                    ds_opts[type_val].forEach(function (entry) {
                        $ds_select.append($($sel_opt.html()).text(entry.name || entry));
                    });                        
                },
                populate_marketdatasnapshots = function () {
                    og.api.rest.marketdatasnapshots.get().pipe(function (resp) {
                        if (resp) ds_opts[type_val] = resp.data[0].snapshots;
                    }).pipe(populate_ds_opts);            
                },
                populate_livedatasources = function () {
                    og.api.rest.livedatasources.get().pipe(function (resp) {
                        if (resp) ds_opts[type_val] = resp.data;
                    }).pipe(populate_ds_opts);
                },
                select_handler = function (entry) {
                    switch(type_val) {
                        case 'Snapshot': populate_marketdatasnapshots(); break;
                        case 'Live': populate_livedatasources(); break;
                    }
                },
                add_handler = function () {
                    menu.add_handler(); 
                },
                menu_handler = function (event) {
                    var target = event.srcElement || event.target,
                        elem = $(target), entry;
                        $sel_parent = elem.parents(options_s);
                        $type_select = $sel_parent.find(type_s);
                        $ds_select = $sel_parent.find(ds_s);
                        $sel_checkbox = $sel_parent.find(checkbox_s);
                        type_val = $type_select.val();
                        ds_val = $ds_select.val();
                        sel_pos = $sel_parent.data('pos');
                        entry = ds_opts.pluck('pos').indexOf(sel_pos);
                    if (elem.is(menu.$dom.add)) return add_handler();
                    if (elem.is(del_s)) return  del_handler(entry);
                    if (elem.is($sel_checkbox)) return checkbox_handler(entry); 
                    if (elem.is(select_s+'.'+type_s)) return select_handler(entry);
                };
            if ($dom) {
                if ($dom.title) $dom.title.on('click', menu.title_handler.bind(menu));
                if ($dom.menu) {
                    $dom.menu.on('click', menu_click_s, menu_handler).on('change', menu_handler);
                }
            }
            return menu;
        };
    }
});