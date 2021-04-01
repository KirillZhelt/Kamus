export default function AddRepositoryItem(props) {
    return (
        <p className='repository-name'>{props.repo.owner} / {props.repo.name} </p>
    );
}