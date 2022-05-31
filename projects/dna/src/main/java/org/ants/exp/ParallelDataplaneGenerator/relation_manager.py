import re
import base64


class Ip:
    def __init__(self, octet0, octet1, octet2, octet3):
        self.octect0 = octet0
        self.octect1 = octet1
        self.octect2 = octet2
        self.octect3 = octet3

    def __hash__(self):
        return hash((self.octect0, self.octect1, self.octect2, self.octect3))

    def __eq__(self, other):
        return (self.octect0, self.octect1, self.octect2, self.octect3) == (
            other.octect0, other.octect1, other.octect2, other.octect3)

    def __repr__(self):
        return 'Ip{{{}, {}, {}, {}}}'.format(self.octect0, self.octect1, self.octect2, self.octect3)

    def to_address(self):
        return '{}.{}.{}.{}'.format(self.octect0, self.octect1, self.octect2, self.octect3)


class Prefix:
    def __init__(self, ip, mask):
        self.ip = ip
        self.mask = mask
        self.net = prefix_to_net(str(self))

    def __eq__(self, other):
        return self.net == other.net

    def __repr__(self):
        return 'Prefix{{{}, {}}}'.format(self.ip, self.mask)


class VNode:
    def __init__(self, device, vrf):
        self.device = device
        self.vrf = vrf

    def __repr__(self):
        return 'VNode{{"{}", "{}"}}'.format(self.device, self.vrf)

    def __eq__(self, other):
        return self.device == other.device and self.vrf == other.vrf


class BaseRelation:
    pass


class Node(BaseRelation):
    def __init__(self, node, as_number, route_id):
        self.node = node
        self.as_number = as_number
        self.id = route_id
        super().__init__()

    def __repr__(self):
        return 'Node({}, {}, {})'.format(self.node, self.as_number, self.id)


class Interface(BaseRelation):
    def __init__(self, node, intf, prefix):
        self.node = node
        self.intf = intf
        self.prefix = prefix

    def __repr__(self):
        return 'Interface({}, "{}", {})'.format(self.node, self.intf, self.prefix)


class L3Link(BaseRelation):
    def __init__(self, node1, intf1, node2, intf2):
        self.node1 = node1
        self.intf1 = intf1
        self.node2 = node2
        self.intf2 = intf2

    def __repr__(self):
        return 'L3Link("{}", "{}", "{}", "{}")'.format(self.node1, self.intf1, self.node2, self.intf2)


class StaticRoute:
    def __init__(self, node, prefix, next_hop_ip, adminCost):
        self.node = node
        self.prefix = prefix
        self.next_hop_ip = next_hop_ip
        self.adminCost = adminCost

    def __repr__(self):
        return 'StaticRoute({}, {}, {}, {})'.format(self.node, self.prefix, self.next_hop_ip, self.adminCost)


class OspfRelation:
    pass


class OspfIntfSetting(OspfRelation):
    def __init__(self, node, intf, cost, area, passive, process):
        self.node = node
        self.intf = intf
        self.cost = cost
        self.area = area
        self.passive = passive
        self.process = process

    def __repr__(self):
        return 'OspfIntfSetting({}, "{}", {}, {}, {}, {})'.format(self.node, self.intf, self.cost, self.area, self.passive, self.process)


class OspfNeighbor(OspfRelation):
    def __init__(self, node1, ip1, node2, ip2):
        self.node1 = node1
        self.ip1 = ip1
        self.node2 = node2
        self.ip2 = ip2

    def __repr__(self):
        return 'OspfNeighbor({}, {}, {}, {})'.format(self.node1, self.ip1, self.node2, self.ip2)


class OspfRedis(OspfRelation):
    def __init__(self, node,  protocol, process):
        self.node = node
        self.protocol = protocol
        self.process = process

    def __repr__(self):
        return 'OspfRedis({}, "{}", {})'.format(self.node, self.protocol, self.process)


class OspfMultipath(OspfRelation):
    def __init__(self, node,  n_paths):
        self.node = node
        self.n_paths = n_paths

    def __repr__(self):
        return 'OspfMultipath({}, "{}")'.format(self.node, self.n_paths)


class BgpRelation:
    pass


class BgpNetwork(BgpRelation):
    def __init__(self, node, prefix):
        self.node = node
        self.prefix = prefix

    def __repr__(self):
        return 'BgpNetwork({}, {})'.format(self.node, self.prefix)


class IBgpNeighbor(BgpRelation):
    def __init__(self, node1, ip1, node2, ip2):
        self.node1 = node1
        self.ip1 = ip1
        self.node2 = node2
        self.ip2 = ip2

    def __repr__(self):
        return 'IBgpNeighbor({}, {}, {}, {})'.format(self.node1, self.ip1, self.node2, self.ip2)


class BgpNeighbor(BgpRelation):
    def __init__(self, node1, ip1, node2, ip2):
        self.node1 = node1
        self.ip1 = ip1
        self.node2 = node2
        self.ip2 = ip2

    def __repr__(self):
        return 'BgpNeighbor({}, {}, {}, {})'.format(self.node1, self.ip1, self.node2, self.ip2)


class BgpMultipath(BgpRelation):
    def __init__(self, node, num, relax):
        self.node = node
        self.num = num
        self.relax = relax

    def __repr__(self):
        return 'BgpMultipath({}, {}, {})'.format(self.node, self.num, self.relax)


class BgpReflectClient(BgpRelation):
    def __init__(self, node1, ip1, node2, ip2):
        self.node1 = node1
        self.ip1 = ip1
        self.node2 = node2
        self.ip2 = ip2

    def __repr__(self):
        return 'BgpReflectClient({}, {}, {}, {})'.format(self.node1, self.ip1, self.node2, self.ip2)


class BgpAggregation(BgpRelation):
    def __init__(self, node, prefix):
        self.node = node
        self.prefix = prefix

    def __repr__(self):
        return 'BgpAggregation({}, {})'.format(self.node, self.prefix)


class BgpRedis(BgpRelation):
    def __init__(self, node, protocol):
        self.node = node
        self.protocol = protocol


class Community:
    def __init__(self, as_number, tag):
        self.as_number = as_number.replace('\\', '\\\\')
        self.tag = tag

    def __repr__(self):
        return 'Community{{"{}", "{}"}}'.format(self.as_number, self.tag)


class MatchCommunity:
    def __init__(self, community):
        self.community = community

    def __repr__(self):
        return 'MatchCommunity{{{}}}'.format(self.community)


class CommonPrefix:
    def __init__(self, prefixes):
        self.prefixes = prefixes

    def __repr__(self):
        return 'CommonPrefix{{{}}}'.format(self.prefixes)


class ExtendPrefix:
    def __init__(self, prefixes):
        self.prefixes = prefixes

    # def __repr__(self):
    #     return 'ExtendPrefix{{{}}}'.format(self.prefixes)


class MatchPrefixList:
    def __init__(self, prefix_lists):
        self.prefix_lists = prefix_lists

    def __repr__(self):
        return 'MatchPrefixList{{{}}}'.format(self.prefix_lists)


class SetCommunity:
    def __init__(self, additive, community):
        self.additive = additive
        self.community = community

    def __repr__(self):
        return 'SetCommunity{{{}, {}}}'.format(self.additive, self.community)


class SetLocalPref:
    def __init__(self, pref):
        self.pref = pref

    def __repr__(self):
        return 'SetLocalPref{{{}}}'.format(self.pref)


class SetMed:
    def __init__(self, metric):
        self.metric = metric

    def __repr__(self):
        return 'SetMed{{{}}}'.format(self.metric)


class RouteMap:
    def __init__(self, permit, matches, actions):
        self.permit = permit
        self.matches = matches
        self.actions = actions

    def __repr__(self):
        return 'RouteMap{{{}, {}, {}}}'.format(self.permit, self.matches, self.actions)


class RouteMapIn(BgpRelation):
    def __init__(self, node, from_node, policies):
        self.node = node
        self.from_node = from_node
        self.policies = policies

    def __repr__(self):
        return 'RouteMapIn({}, {}, {})'.format(self.node, self.from_node, self.policies)


class RouteMapOut(BgpRelation):
    def __init__(self, node, from_node, policies):
        self.node = node
        self.from_node = from_node
        self.policies = policies

    def __repr__(self):
        return 'RouteMapOut({}, {}, {})'.format(self.node, self.from_node, self.policies)


class ConnectedRoute:
    def __init__(self, node, prefix, intf):
        self.node = node
        self.prefix = prefix
        self.intf = intf

    def __repr__(self):
        return 'ConnectedRoute({}, {}, "{}")'.format(self.node, self.prefix, self.intf)


class L3Neighbor:
    def __init__(self, node1, intf1, ip1, node2, intf2, ip2):
        self.node1 = node1
        self.intf1 = intf1
        self.ip1 = ip1
        self.node2 = node2
        self.intf2 = intf2
        self.ip2 = ip2

    def __repr__(self):
        return 'L3Neighbor({}, "{}", {}, {}, "{}", {})'.format(
            self.node1, self.intf1, self.ip1, self.node2, self.intf2, self.ip2)

class BaseGlobalRib:
    def __init__(self, node, prefix, intf, next_hop_ip, protocol, admin):
        self.node = node
        self.prefix = prefix
        self.intf = intf
        self.next_hop_ip = next_hop_ip
        self.protocol = protocol
        self.admin = admin

    def __repr__(self):
        return 'BaseGlobalRib({}, {}, "{}", {}, "{}", {})'.format(
            self.node, self.prefix, self.intf, self.next_hop_ip, self.protocol, self.admin)


class OspfRib:
    def __init__(self, node, prefix, next_hop, next_hop_ip, cost):
        self.node = node
        self.prefix = prefix
        self.next_hop = next_hop
        self.next_hop_ip = next_hop_ip
        self.cost = cost

    def __repr__(self):
        return 'OspfRib({}, {}, "{}", {}, {})'.format(
            self.node, self.prefix, self.next_hop, self.next_hop_ip, self.cost)


# Prefix{Ip{1,1,1,1}, Ip{255,255,0,0}} -> Prefix{Ip{1,1,0,0}, Ip{255,255,0,0}}
def prefix_to_net(prefix):
    ip, mask = re.findall('Ip{(.*?)}', prefix)
    ip_t = ip.split(',')
    mask_t = mask.split(',')
    net = ','.join([str(int(ioctet) & int(moctet)) for ioctet, moctet in zip(ip_t, mask_t)])
    return prefix.replace(ip, net)


def instantiate(x, args):
    return globals()[x](*args)  # not safe


def parse(str_relation):
    # preprocess, use base64 to protect some regex in route map
    encoded_relation = re.sub(
        r'\".*?\"', lambda x: base64.b64encode(x.group().encode()).decode() + '!', str_relation)
    splited_relation = re.split(
        r'([(){}\[\],])', re.sub(r'\s+', '', encoded_relation))
    decoded_relation = []
    for i in splited_relation:
        if i.endswith('!'):
            i = base64.b64decode(i.encode()).decode()
        if i in ['true', 'false']:
            i = '"{}"'.format(i)
        decoded_relation.append(i)

    # a parser start from tail
    to_instance = False
    args = []
    n_args = [0]  # record number of arguments for each scope
    n_list = [0]
    for item in reversed(decoded_relation):
        if item != '' and item != ',':
            if item == ')' or item == '}':  # start a new scope
                n_args.append(0)
                n_list.append(0)
            elif item == '(' or item == '{':  # ready to end a scope
                n_args[-2] += 1
                n_list[-2] += 1
                to_instance = True
            elif item == ']':  # start a list
                n_list.append(0)
            elif item == '[':  # end a list
                l = args[len(args) - n_list[-1]:][::-1]
                args = args[:len(args) - n_list[-1]]
                args.append(l)
                n_args[-1] -= (len(l) - 1)
                n_list[-2] += 1
                n_list.pop()
            elif to_instance:  # instantiate a object and end this scope
                # print(args, args[len(args) - n_args[-1]:][::-1])
                obj = instantiate(item, (args[len(args) - n_args[-1]:])[::-1])
                args = args[:len(args) - n_args[-1]]
                n_args.pop()
                args.append(obj)
                to_instance = False
                n_list.pop()
            else:  # add an argument for current scope
                args.append(eval(item))  # ast.literal_eval()?
                n_args[-1] += 1
                n_list[-1] += 1

    assert len(args) == 1
    return args[0]


def read_relations(path):
    relations = []
    with open(path, 'r') as file:
        for line in file.readlines():
            if line.startswith('insert'):
                relation = parse(line.replace('insert ', ''))
                relations.append(relation)
    return relations


def test():
    # s = 'Interface("node0", "GigabitEthernet1/0", Prefix{Ip{1, 0, 0, 1}, Ip{255, 255, 255, 0}})'
    # s = 'Interface("node0", "GigabitEthernet1/0", Prefix{Ip{1, 0, 0, 1}, [1, 2, [1, 2], [1, 2]]})'
    # s = r'RouteMapIn("as3border2", "as1border2", [RouteMap{true, [MatchCommunity{Community{"(,|\\{|\\}|^|$|1)", ".*"}}], [SetCommunity{true, Community{"3", "2"}}, SetLocalPref{350}]}])'

    s = 'Node(VNode{"core-13","default"},13,1174408448)'
    obj = parse(s)
    assert isinstance(obj, Node) == True


if __name__ == "__main__":
    test()
