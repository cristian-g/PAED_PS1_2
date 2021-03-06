import java.util.*;

public class RepartirUsuaris {

    private final Server[] servers;
    private final User[] users;

    public RepartirUsuaris(Server[] servers, User[] users) {
        this.servers = servers.clone();
        this.users = users.clone();
        for (User user : this.users) {
            Post[] posts = user.getPosts();
            Arrays.sort(posts, (o1, o2) -> Long.compare(o2.getPublished(), o1.getPublished()));
            user.setPosts(posts);
        }
    }

    public UserSolution backtracking(UserSolution s, ArrayList<UserSolution> solutions, double[] minEquity, boolean getResult) {
        if (s != null) {
            s = new UserSolution(s);
        } else {
            s = new UserSolution(servers);
        }

        if (solutions == null) {
            solutions = new ArrayList<>();
        }

        if (minEquity == null) {
            minEquity = new double[] {-1};
        }

        if (isSolution(s)) {
            if (isPromising(s, minEquity[0])) {
                solutions.add(s);

                if (minEquity[0] == -1 || s.getEquity() < minEquity[0]) {
                    minEquity[0] = s.getEquity();
                }


                for (int i = 0; i < solutions.size(); ++i) {
                    if (!isPromising(solutions.get(i), minEquity[0])) {
                        solutions.remove(i);
                        --i;
                    }
                }
            }
        } else {
            User u = users[s.getTotalUsers()];
            for (Server opt:s.getServers()) {
                s.add(u, opt);
                backtracking(s, solutions, minEquity, false);
                s.remove(u, opt);
            }
        }

        if (getResult) {
            UserSolution best = null;
            for (UserSolution us : solutions) {
                if (best == null) {
                    best = us;
                } else {
                    if (best.getDistTotal() > us.getDistTotal()) {
                        best = us;
                    }
                }
            }
            return best;
        } else {
            return null;
        }
    }

    private boolean isPromising(UserSolution option, double minEquity) {
        if (minEquity == -1) {
            return true;
        } else {
            return option.getEquity() <= minEquity * 1.1;
        }
    }

    UserSolution branchAndBound(double minEquity) {
        PriorityQueue<UserSolution> liveNodes = new PriorityQueue<>(11, (o1, o2) -> Double.compare(o1.getEquity(), o2.getEquity()));
        ArrayList<UserSolution> solutions = new ArrayList<>();
        UserSolution x = new UserSolution(servers);

        liveNodes.add(x);

        while (liveNodes.size() > 0) {
            x = liveNodes.poll();
            ArrayList<UserSolution> options = expand(x);

            for (UserSolution s : options) {
                if (isSolution(s)) {
                    if (isPromising(s, minEquity)) {
                        solutions.add(s);

                        if (minEquity == -1 || s.getEquity() < minEquity) {
                            minEquity = s.getEquity();
                        }


                        for (int i = 0; i < solutions.size(); ++i) {
                            if (!isPromising(solutions.get(i), minEquity)) {
                                solutions.remove(i);
                                --i;
                            }
                        }
                    }
                } else  {
                    liveNodes.add(s);
                }
            }
        }

        UserSolution best = null;
        for (UserSolution us : solutions) {
            if (best == null) {
                best = us;
            } else {
                if (best.getDistTotal() > us.getDistTotal()) {
                    best = us;
                }
            }
        }
        return best;
    }

    private boolean isSolution(UserSolution option) {
        return option.getTotalUsers() == users.length;
    }

    ArrayList<UserSolution> expand(UserSolution us) {
        ArrayList<UserSolution> solutions = new ArrayList<>();
        UserSolution s;

        for (Server sv:us.getServers()) {
            s = new UserSolution(us);
            s.add(users[s.getTotalUsers()], sv);
            solutions.add(s);
        }

        return solutions;
    }

    UserSolution greedy() {
        User[] users = this.users.clone();
        Server[] servers = this.servers;
        UserSolution s = new UserSolution(servers);

        Arrays.sort(users, (o1, o2) -> Double.compare(o2.getActivity(), o1.getActivity()));

        ArrayList<User> candidats = new ArrayList<>(Arrays.asList(users));

        while (candidats.size() > 0) {
            s.add(candidats.get(0), servers[0]);
            candidats.remove(0);
            Server[] solSer = s.getServers();
            Arrays.sort(solSer, new Comparator<Server>() {
                @Override
                public int compare(Server o1, Server o2) {
                    return Double.compare(o1.getLoad(), o2.getLoad());
                }
            });
            servers = solSer;
        }

        return s;
    }
}
