<%inherit file="/base.mako"/>
<%namespace file="/message.mako" import="render_msg" />

<% import os %>

%if message:
    ${render_msg( message, status )}
%endif

<div class="toolForm">
    <div class="toolFormTitle">Uninstall tool dependencies</div>
    <div class="toolFormBody">
        <form name="uninstall_tool_dependenceies" id="uninstall_tool_dependenceies" action="${h.url_for( controller='admin_toolshed', action='uninstall_tool_dependencies' )}" method="post" >       
            <div class="form-row">
                <table class="grid">
                    <tr>
                        <th>Name</th>
                        <th>Version</th>
                        <th>Type</th>
                        <th>Install directory</th>
                    </tr>
                    %for tool_dependency in tool_dependencies:
                        <input type="hidden" name="tool_dependency_ids" value="${trans.security.encode_id( tool_dependency.id )}"/>
                        <%
                            install_dir = os.path.join( trans.app.config.tool_dependency_dir,
                                                        tool_dependency.name,
                                                        tool_dependency.version,
                                                        tool_dependency.tool_shed_repository.owner,
                                                        tool_dependency.tool_shed_repository.name,
                                                        tool_dependency.tool_shed_repository.installed_changeset_revision )
                        %>
                        %if os.path.exists( install_dir ):
                            <tr>
                                <td>${tool_dependency.name}</td>
                                <td>${tool_dependency.version}</td>
                                <td>${tool_dependency.type}</td>
                                <td>${install_dir}</td>
                            </tr>
                        %endif
                    %endfor
                </table>
                <div style="clear: both"></div>
            </div>
            <div class="form-row">
                <input type="submit" name="uninstall_tool_dependencies_button" value="Uninstall"/>
                <div class="toolParamHelp" style="clear: both;">
                    Click to uninstall the tool dependencies listed above.
                </div>
            </div>
        </form>
    </div>
</div>
