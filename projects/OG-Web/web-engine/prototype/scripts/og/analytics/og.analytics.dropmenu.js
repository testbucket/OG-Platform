$.register_module({
    name: 'og.analytics.DropMenu',
    dependencies: [],
    obj: function () {
        var events = {
                focus: 'dropmenu:focus',
                focused:'dropmenu:focused',
                open: 'dropmenu:open',
                opened: 'dropmenu:opened',
                close: 'dropmenu:close',
                closed: 'dropmenu:closed'
            },
            /**
             * TODO AG: Must provide Getters/Setters for instance properties as these should really be private
             * and not accessible directly via the instance.
             */
            DropMenu = function (config) {
                var menu = this, tmpl = config.tmpl, dummy_s = '<div>', data = config.data || {};
                menu.state = 'closed';
                menu.opened = false;
                menu.data = data;
                menu.$dom = {};
                menu.$dom.cntr = config.$cntr.html($((Handlebars.compile(tmpl))(data)));
                menu.$dom.title = $('.og-option-title', menu.$dom.cntr);
                menu.$dom.title_prefix = $(dummy_s);
                menu.$dom.title_infix = $(dummy_s);
                menu.$dom.menu = $('.OG-analytics-form-menu', menu.$dom.cntr);
                menu.$dom.menu_actions = $('.OG-dropmenu-actions', menu.$dom.menu);
                menu.$dom.opt = $('.OG-dropmenu-options', menu.$dom.menu);
                menu.$dom.opt.data('pos', ((menu.opts = []).push(menu.$dom.opt), menu.opts.length-1));
                menu.$dom.add = $('.OG-link-add', menu.$dom.menu);
                menu.$dom.opt_cp = menu.$dom.opt.clone(true);
                menu.addListener(events.open, menu.open.bind(menu))
                    .addListener(events.close, menu.close.bind(menu))
                    .addListener(events.focus, menu.focus.bind(menu));
                return menu;
            };
        $.extend(true, DropMenu.prototype, EventEmitter.prototype);
        DropMenu.prototype.constructor = DropMenu;
        DropMenu.prototype.focus = function () {
            var menu = this;
            return menu.opts[menu.opts.length-1].find('select').first().focus(), menu.opened = true,
                menu.state = 'focused', menu.emitEvent(events.focused, [menu]), menu;
        };
        DropMenu.prototype.open = function () {
            var menu = this;
            return menu.$dom.menu.show().blurkill(menu.close.bind(menu)), menu.state = 'open', menu.opened = true,
                menu.$dom.title.addClass('og-active'), menu.emitEvent(events.opened, [menu]), menu;
        };
        DropMenu.prototype.close = function () {
            var menu = this;
            return (menu.$dom.menu ? menu.$dom.menu.hide() : null), menu.state = 'closed', menu.opened = false,
                menu.$dom.title.removeClass('og-active'), menu.emitEvent(events.closed, [menu]), menu;
        };
        DropMenu.prototype.menu_handler = function (event) {
            var menu = this, elem = $(event.target);
            if (elem.is(menu.$dom.add)) {
                menu.add_handler(); 
                menu.stop(event);
            }
            if (elem.is('.og-icon-delete')) {
                menu.del_handler(elem.closest('.OG-dropmenu-options'));
                menu.stop(event);
            }
            if (elem.is(':checkbox')) {elem.focus();}
        };
        DropMenu.prototype.title_handler = function () {
            var menu = this;
                menu.opened ? menu.close() : (menu.open(), menu.focus());
        };
        DropMenu.prototype.add_handler = function () {
            var menu = this, opt, len = menu.opts.length;
            return opt = menu.$dom.opt_cp.clone(true).data("pos", menu.opts.length), menu.opts.push(opt),
                    menu.$dom.add.focus(), menu.opts[len].find('.number span').text(menu.opts.length), 
                    menu.$dom.menu_actions.before(menu.opts[len]);
        };
        DropMenu.prototype.del_handler = function (elem) {
            var menu = this, data = elem.data();
            if (menu.opts.length === 1) return;
            menu.opts.splice(data.pos, 1);
            elem.remove();
            menu.update_opt_nums(data.pos);
            if (data.pos < menu.opts.length) menu.opts[data.pos].find('select').first().focus();
            else menu.opts[data.pos-1].find('select').first().focus();
        };
        DropMenu.prototype.update_opt_nums = function (pos) {
            var menu = this;
            for (var i = pos || 0, len = menu.opts.length; i < len;)
                menu.opts[i].data('pos', i).find('.number span').text(i+=1);
        };
        DropMenu.prototype.stop = function (event) {
            event.stopPropagation();
            event.preventDefault();
        };
        DropMenu.prototype.status = function () {
            var menu = this;
            return menu.state;
        };
        DropMenu.prototype.is_opened = function () {
            var menu = this;
            return menu.opened;
        };
        return DropMenu;
    }
});